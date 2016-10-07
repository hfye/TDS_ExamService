package tds.exam.services.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import tds.assessment.SetOfAdminSubject;
import tds.common.Response;
import tds.common.ValidationError;
import tds.common.data.legacy.LegacyComparer;
import tds.config.ClientTestProperty;
import tds.exam.Exam;
import tds.exam.ExamStatusCode;
import tds.exam.OpenExamRequest;
import tds.exam.error.ValidationErrorCode;
import tds.exam.models.Ability;
import tds.exam.repositories.ExamQueryRepository;
import tds.exam.repositories.HistoryQueryRepository;
import tds.exam.services.AssessmentService;
import tds.exam.services.ExamService;
import tds.exam.services.SessionService;
import tds.exam.services.StudentService;
import tds.session.ExternalSessionConfiguration;
import tds.session.Session;
import tds.student.Student;

import static java.time.temporal.ChronoUnit.DAYS;

@Service
class ExamServiceImpl implements ExamService {
    private static final Logger LOG = LoggerFactory.getLogger(ExamServiceImpl.class);

    private final ExamQueryRepository examQueryRepository;
    private final HistoryQueryRepository historyQueryRepository;
    private final SessionService sessionService;
    private final StudentService studentService;
    private final AssessmentService assessmentService;

    @Autowired
    public ExamServiceImpl(ExamQueryRepository examQueryRepository,
                           HistoryQueryRepository historyQueryRepository,
                           SessionService sessionService,
                           StudentService studentService,
                           AssessmentService assessmentService) {
        this.examQueryRepository = examQueryRepository;
        this.historyQueryRepository = historyQueryRepository;
        this.sessionService = sessionService;
        this.studentService = studentService;
        this.assessmentService = assessmentService;
    }

    @Override
    public Optional<Exam> getExam(UUID id) {
        return examQueryRepository.getExamById(id);
    }

    @Override
    public Response<Exam> openExam(OpenExamRequest openExamRequest) {
        Optional<Session> maybeSession = sessionService.findSessionById(openExamRequest.getSessionId());
        if (!maybeSession.isPresent()) {
            throw new IllegalArgumentException(String.format("Could not find session for %s", openExamRequest.getSessionId()));
        }

        if (!openExamRequest.isGuestStudent()) {
            Optional<Student> maybeStudent = studentService.getStudentById(openExamRequest.getStudentId());
            if (!maybeStudent.isPresent()) {
                throw new IllegalArgumentException(String.format("Could not find student for %s", openExamRequest.getStudentId()));
            }
        }

        Session currentSession = maybeSession.get();

        //Previous exam is retrieved in lines 5492 - 5530 and 5605 - 5645 in StudentDLL
        Optional<Exam> maybePreviousExam = examQueryRepository.getLastAvailableExam(openExamRequest.getStudentId(), openExamRequest.getAssessmentId(), openExamRequest.getClientName());

        boolean canOpenPreviousExam = false;
        if (maybePreviousExam.isPresent()) {
            Optional<ValidationError> canOpenPreviousExamError = canOpenPreviousExam(maybePreviousExam.get(), currentSession);

            if (canOpenPreviousExamError.isPresent()) {
                return new Response<Exam>(canOpenPreviousExamError.get());
            }

            canOpenPreviousExam = true;
        }

        Exam exam;
        if (canOpenPreviousExam) {
            //Open previous exam
            LOG.debug("Can open previous exam");
            exam = new Exam.Builder().withId(maybePreviousExam.get().getId()).build();
        } else {
            //Line 5602 in StudentDLL
            Optional<ExternalSessionConfiguration> maybeExternalSessionConfiguration = sessionService.findExternalSessionConfigurationByClientName(openExamRequest.getClientName());

            if (!maybeExternalSessionConfiguration.isPresent()) {
                throw new IllegalStateException("External Session Configuration could not be found for client name " + openExamRequest.getClientName());
            }

            ExternalSessionConfiguration externalSessionConfiguration = maybeExternalSessionConfiguration.get();
            Exam previousExam = maybePreviousExam.isPresent() ? maybePreviousExam.get() : null;
            Optional<ValidationError> maybeOpenNewExam = canCreateNewExam(openExamRequest, previousExam, externalSessionConfiguration);
            if (maybeOpenNewExam.isPresent()) {
                return new Response<Exam>(maybeOpenNewExam.get());
            }

            exam = new Exam.Builder().withId(UUID.randomUUID()).build();
        }

        return new Response<>(exam);
    }

    /**
     * @inheritDoc
     */
    @Override
    public Optional<Float> getInitialAbility(Exam exam, ClientTestProperty property) {
        Float abilityVal = null;
        Double slope = property.getAbilitySlope();
        Double intercept = property.getAbilityIntercept();
        List<Ability> testAbilities = examQueryRepository.findAbilities(exam.getId(), exam.getClientName(),
                property.getSubjectName(), exam.getStudentId());

        // Attempt to retrieve the most recent ability for the current subject and assessment
        Ability initialAbility = getMostRecentTestAbility(testAbilities, exam.getAssessmentId(), false);
        if (initialAbility != null) {
            abilityVal = initialAbility.getScore();
        } else if (property.getInitialAbilityBySubject()) {
            initialAbility = getMostRecentTestAbility(testAbilities, exam.getAssessmentId(), true);
            if (initialAbility != null) { // and if that didn't work, get the initial ability from the previous year.
                abilityVal = initialAbility.getScore();
            } else {
                Optional<Float> initialAbilityFromHistory = historyQueryRepository.findAbilityFromHistoryForSubjectAndStudent(
                        exam.getClientName(), exam.getSubject(), exam.getStudentId());

                if (initialAbilityFromHistory.isPresent() && slope != null && intercept != null) {
                    abilityVal = initialAbilityFromHistory.get() * slope.floatValue() + intercept.floatValue();
                } else if (initialAbilityFromHistory.isPresent()) {
                    // If no slope/intercept is provided, store base value
                    abilityVal = initialAbilityFromHistory.get();
                }
            }
        }

        //If the ability was not retrieved/set from the above logic, grab it from the item bank DB
        if (abilityVal == null) {
            Optional<SetOfAdminSubject> subjectOptional = assessmentService.findSetOfAdminSubjectByKey(exam.getAssessmentId());
            if (subjectOptional.isPresent()) {
                abilityVal = subjectOptional.get().getStartAbility();
            } else {
                LOG.warn("Could not set the ability for exam ID " + exam.getId());
            }
        }

        return Optional.ofNullable(abilityVal);
    }

    private Response<Exam> createExam(OpenExamRequest openExamRequest, Student student, Session session, ExternalSessionConfiguration externalSessionConfiguration) {
        //From OpenTestServiceImpl lines 160 -163
        String examStatus;
        if (openExamRequest.getProctorId() == null) {
            examStatus = "approved";
        } else {
            examStatus = "pending";
        }

        Instant startTime = Instant.now();


        return null;
    }

    /**
     * Gets the most recent {@link Ability} based on the dateScored value
     *
     * @param abilityList the list of {@link Ability}s to iterate through
     * @param assessmentId  The test key
     * @param matchesAssessment Specifies whether to search for matches or non-matches of the assessment key
     * @return
     */
    private Ability getMostRecentTestAbility(List<Ability> abilityList, String assessmentId, boolean matchesAssessment) {
        Ability mostRecentAbility = null;

        for (Ability ability : abilityList) {
            if (matchesAssessment) {
                if (!assessmentId.equals(ability.getAssessment())) {
                    if (mostRecentAbility == null || mostRecentAbility.getDateScored().isBefore(ability.getDateScored())) {
                        mostRecentAbility = ability;
                    }
                }
            } else {
                if (assessmentId.equals(ability.getAssessment())) {
                    if (mostRecentAbility == null || mostRecentAbility.getDateScored().isBefore(ability.getDateScored())) {
                        mostRecentAbility = ability;
                    }
                }
            }
        }

        return mostRecentAbility;
    }

    private Optional<ValidationError> canOpenPreviousExam(Exam previousExam, Session currentSession) {
        //Port of Student.DLL lines 5526-5530
        if (ExamStatusCode.STAGE_CLOSED.equals(previousExam.getStatus().getStage())) {
            return Optional.empty();
        }

        //Port of Student.DLL lines 5531-5551
        //If either session type is null or if they don't match an error is returned
        Optional<Session> maybePreviousSession = sessionService.findSessionById(previousExam.getSessionId());
        if (!maybePreviousSession.isPresent()) {
            return Optional.of(new ValidationError(ValidationErrorCode.SESSION_TYPE_MISMATCH, "current session type and previous session type don't match"));
        }

        Session previousSession = maybePreviousSession.get();
        if (previousSession.getType() != currentSession.getType()) {
            return Optional.of(new ValidationError(ValidationErrorCode.SESSION_TYPE_MISMATCH, "current session type and previous session type don't match"));
        }

        //Port of Student.DLL lines 5555-5560
        if (ExamStatusCode.STAGE_INACTIVE.equals(previousExam.getStatus().getStage())) {
            return Optional.empty();
        }

        /*
        The below code is a straight port of the legacy StudentDLL._CanOpenExistingOpportunity_SP.  It was a little hard to follow the application
        reasons for this logic. StudentDLL lines 5569 - 5589
         */
        boolean daysSinceLastChange = false;
        if (previousExam.getDateChanged() != null) {
            daysSinceLastChange = DAYS.between(previousExam.getDateChanged(), Instant.now()) >= 1;
        }

        if (daysSinceLastChange ||
            LegacyComparer.isEqual(previousSession.getId(), currentSession.getId()) ||
            LegacyComparer.isEqual("closed", previousSession.getStatus()) ||
            LegacyComparer.greaterThan(Instant.now(), previousSession.getDateEnd())) {
            return Optional.empty();
        }

        //Port of Student.DLL line 5593
        return Optional.of(new ValidationError(ValidationErrorCode.CURRENT_EXAM_OPEN, "Current exam is active"));
    }

    private Optional<ValidationError> canCreateNewExam(OpenExamRequest openExamRequest, Exam previousExam, ExternalSessionConfiguration externalSessionConfiguration) {

        //Lines 5612 - 5618 in StudentDLL
        if (previousExam == null) {
            if (openExamRequest.getMaxAttempts() < 0 && LegacyComparer.notEqual("SIMULATION", externalSessionConfiguration.getEnvironment())) {
                return Optional.of(new ValidationError(ValidationErrorCode.SIMULATION_ENVIRONMENT_REQUIRED, "Environment must be simulation when max attempts less than zero"));
            }

            return Optional.empty();
        }

        //Lines 5645 - 5673 in StudentDLL
        if (ExamStatusCode.STAGE_CLOSED.equals(previousExam.getStatus().getStage())) {
            if (LegacyComparer.isEqual("SIMULATION", externalSessionConfiguration.getEnvironment())) {
                return Optional.empty();
            }

            if (previousExam.getDateCompleted() != null) {
                Duration duration = Duration.between(previousExam.getDateChanged(), Instant.now());
                if (LegacyComparer.lessThan(previousExam.getAttempts(), openExamRequest.getMaxAttempts()) &&
                    LegacyComparer.greaterThan(duration.get(DAYS), openExamRequest.getNumberOfDaysToDelay())) {
                    return Optional.empty();
                } else if (LegacyComparer.greaterOrEqual(previousExam.getAttempts(), openExamRequest.getMaxAttempts())) {
                    return Optional.of(new ValidationError(ValidationErrorCode.MAX_OPPORTUNITY_EXCEEDED, "Max number of attempts for exam exceeded"));
                } else {
                    return Optional.of(new ValidationError(ValidationErrorCode.NOT_ENOUGH_DAYS_PASSED, String.format("Next exam cannot be started until %s days pass since last exam", openExamRequest.getNumberOfDaysToDelay())));
                }
            }
        }

        return Optional.empty();
    }
}
