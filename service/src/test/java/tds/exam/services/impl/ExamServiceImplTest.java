package tds.exam.services.impl;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import tds.assessment.SetOfAdminSubject;
import tds.common.Response;
import tds.common.ValidationError;
import tds.config.ClientTestProperty;
import tds.exam.Exam;
import tds.exam.ExamStatusCode;
import tds.exam.OpenExamRequest;
import tds.exam.error.ValidationErrorCode;
import tds.exam.models.Ability;
import tds.exam.repositories.ExamQueryRepository;
import tds.exam.repositories.HistoryQueryRepository;
import tds.exam.services.AssessmentService;
import tds.exam.services.SessionService;
import tds.exam.services.StudentService;
import tds.session.ExternalSessionConfiguration;
import tds.session.Session;
import tds.student.Student;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class ExamServiceImplTest {
    private ExamQueryRepository repository;
    private HistoryQueryRepository historyRepository;
    private ExamServiceImpl examService;
    private SessionService sessionService;
    private StudentService studentService;
    private AssessmentService assessmentService;

    @Before
    public void setUp() {
        repository = mock(ExamQueryRepository.class);
        historyRepository = mock(HistoryQueryRepository.class);
        sessionService = mock(SessionService.class);
        studentService = mock(StudentService.class);
        assessmentService = mock(AssessmentService.class);
        examService = new ExamServiceImpl(repository, historyRepository, sessionService, studentService, assessmentService);
    }

    @After
    public void tearDown() {
    }

    @Test
    public void shouldReturnAnExam() {
        UUID examId = UUID.randomUUID();
        when(repository.getExamById(examId)).thenReturn(Optional.of(new Exam.Builder().withId(examId).build()));

        assertThat(examService.getExam(examId).get().getId()).isEqualTo(examId);
    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldReturnErrorWhenSessionCannotBeFound() {
        UUID sessionId = UUID.randomUUID();
        OpenExamRequest openExamRequest = new OpenExamRequest();
        openExamRequest.setSessionId(sessionId);
        openExamRequest.setStudentId(1);

        when(sessionService.findSessionById(sessionId)).thenReturn(Optional.empty());
        when(studentService.getStudentById(1)).thenReturn(Optional.of(new Student(1, "testId", "CA", "clientName")));

        examService.openExam(openExamRequest);
    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldReturnErrorWhenStudentCannotBeFound() {
        UUID sessionId = UUID.randomUUID();
        OpenExamRequest openExamRequest = new OpenExamRequest();
        openExamRequest.setStudentId(1);
        openExamRequest.setSessionId(sessionId);

        when(sessionService.findSessionById(sessionId)).thenReturn(Optional.of(new Session.Builder().build()));
        when(studentService.getStudentById(1)).thenReturn(Optional.empty());

        examService.openExam(openExamRequest);
    }

    @Test
    public void shouldReturnErrorWhenPreviousSessionTypeDoesNotEqualCurrentSessionType() {
        UUID sessionId = UUID.randomUUID();
        OpenExamRequest openExamRequest = new OpenExamRequest();
        openExamRequest.setStudentId(1);
        openExamRequest.setSessionId(sessionId);
        openExamRequest.setAssessmentId("assessmentId");
        openExamRequest.setClientName("SBAC-PT");

        Session currentSession = new Session.Builder()
            .withType(2)
            .build();

        Session previousSession = new Session.Builder()
            .withId(UUID.randomUUID())
            .withType(33)
            .build();

        Student student = new Student(1, "testId", "CA", "clientName");

        Exam previousExam = new Exam.Builder()
            .withId(UUID.randomUUID())
            .withSessionId(previousSession.getId())
            .withStatus(new ExamStatusCode.Builder().withStage(ExamStatusCode.STAGE_INACTIVE).build())
            .build();

        when(sessionService.findSessionById(sessionId)).thenReturn(Optional.of(currentSession));
        when(studentService.getStudentById(1)).thenReturn(Optional.of(student));
        when(repository.getLastAvailableExam(1, "assessmentId", "SBAC-PT")).thenReturn(Optional.of(previousExam));
        when(sessionService.findSessionById(previousSession.getId())).thenReturn(Optional.of(previousSession));

        Response<Exam> examResponse = examService.openExam(openExamRequest);

        assertThat(examResponse.getData()).isNotPresent();
        assertThat(examResponse.getErrors().get()).hasSize(1);

        ValidationError validationError = examResponse.getErrors().get()[0];
        assertThat(validationError.getCode()).isEqualTo(ValidationErrorCode.SESSION_TYPE_MISMATCH);
    }

    @Test(expected = IllegalStateException.class)
    public void shouldThrowIllegalStateIfExternSessionConfigCannotBeFound() {
        UUID sessionId = UUID.randomUUID();
        OpenExamRequest openExamRequest = new OpenExamRequest();
        openExamRequest.setStudentId(1);
        openExamRequest.setSessionId(sessionId);
        openExamRequest.setAssessmentId("assessmentId");
        openExamRequest.setClientName("SBAC-PT");

        Session currentSession = new Session.Builder()
            .withType(2)
            .build();

        Student student = new Student(1, "testId", "CA", "clientName");

        when(sessionService.findSessionById(sessionId)).thenReturn(Optional.of(currentSession));
        when(studentService.getStudentById(1)).thenReturn(Optional.of(student));
        when(repository.getLastAvailableExam(1, "assessmentId", "SBAC-PT")).thenReturn(Optional.empty());
        when(sessionService.findExternalSessionConfigurationByClientName("SBAC-PT")).thenReturn(Optional.empty());
        examService.openExam(openExamRequest);
    }

    @Test
    public void shouldReturnErrorWhenMaxOpportunitiesLessThanZeroAndEnvironmentNotSimulation() {
        UUID sessionId = UUID.randomUUID();
        OpenExamRequest openExamRequest = new OpenExamRequest();
        openExamRequest.setStudentId(1);
        openExamRequest.setSessionId(sessionId);
        openExamRequest.setAssessmentId("assessmentId");
        openExamRequest.setClientName("SBAC-PT");
        openExamRequest.setMaxAttempts(-1);

        Session currentSession = new Session.Builder()
            .withType(2)
            .build();

        Session previousSession = new Session.Builder()
            .withId(UUID.randomUUID())
            .withType(2)
            .build();

        Student student = new Student(1, "testId", "CA", "clientName");

        when(sessionService.findSessionById(sessionId)).thenReturn(Optional.of(currentSession));
        when(studentService.getStudentById(1)).thenReturn(Optional.of(student));
        when(repository.getLastAvailableExam(1, "assessmentId", "SBAC-PT")).thenReturn(Optional.empty());
        when(sessionService.findSessionById(previousSession.getId())).thenReturn(Optional.of(previousSession));
        ExternalSessionConfiguration extSessionConfig = new ExternalSessionConfiguration("SBAC-PT", "Development", 0, 0);
        when(sessionService.findExternalSessionConfigurationByClientName("SBAC-PT")).thenReturn(Optional.of(extSessionConfig));

        Response<Exam> examResponse = examService.openExam(openExamRequest);

        assertThat(examResponse.getData()).isNotPresent();
        assertThat(examResponse.getErrors().get()).hasSize(1);

        ValidationError validationError = examResponse.getErrors().get()[0];
        assertThat(validationError.getCode()).isEqualTo(ValidationErrorCode.SIMULATION_ENVIRONMENT_REQUIRED);
    }

    @Test
    public void shouldNotAllowExamToOpenIfStillActive() {
        UUID sessionId = UUID.randomUUID();
        OpenExamRequest openExamRequest = new OpenExamRequest();
        openExamRequest.setStudentId(1);
        openExamRequest.setSessionId(sessionId);
        openExamRequest.setAssessmentId("assessmentId");
        openExamRequest.setClientName("SBAC-PT");
        openExamRequest.setMaxAttempts(5);

        Session currentSession = new Session.Builder()
            .withType(2)
            .build();

        Session previousSession = new Session.Builder()
            .withId(UUID.randomUUID())
            .withType(2)
            .build();

        Student student = new Student(1, "testId", "CA", "clientName");

        Exam previousExam = new Exam.Builder()
            .withId(UUID.randomUUID())
            .withSessionId(previousSession.getId())
            .withStatus(new ExamStatusCode.Builder().withStage(ExamStatusCode.STAGE_OPEN).build())
            .build();

        when(sessionService.findSessionById(sessionId)).thenReturn(Optional.of(currentSession));
        when(studentService.getStudentById(1)).thenReturn(Optional.of(student));
        when(repository.getLastAvailableExam(1, "assessmentId", "SBAC-PT")).thenReturn(Optional.of(previousExam));
        when(sessionService.findSessionById(previousSession.getId())).thenReturn(Optional.of(previousSession));
        ExternalSessionConfiguration externalSessionConfiguration = new ExternalSessionConfiguration("SBAC-PT", "Development", 0, 0);
        when(sessionService.findExternalSessionConfigurationByClientName("SBAC-PT")).thenReturn(Optional.of(externalSessionConfiguration));

        Response<Exam> examResponse = examService.openExam(openExamRequest);

        assertThat(examResponse.getData()).isNotPresent();
        assertThat(examResponse.getErrors().get()).hasSize(1);

        ValidationError validationError = examResponse.getErrors().get()[0];
        assertThat(validationError.getCode()).isEqualTo(ValidationErrorCode.CURRENT_EXAM_OPEN);
    }

    @Test
    public void shouldAllowPreviousExamToOpenIfDayHasPassed() {
        UUID sessionId = UUID.randomUUID();
        OpenExamRequest openExamRequest = new OpenExamRequest();
        openExamRequest.setStudentId(1);
        openExamRequest.setSessionId(sessionId);
        openExamRequest.setAssessmentId("assessmentId");
        openExamRequest.setClientName("SBAC-PT");
        openExamRequest.setMaxAttempts(5);

        Session currentSession = new Session.Builder()
            .withType(2)
            .build();

        Session previousSession = new Session.Builder()
            .withId(UUID.randomUUID())
            .withType(2)
            .build();

        Student student = new Student(1, "testId", "CA", "clientName");

        Exam previousExam = new Exam.Builder()
            .withId(UUID.randomUUID())
            .withSessionId(previousSession.getId())
            .withStatus(new ExamStatusCode.Builder().withStage(ExamStatusCode.STAGE_OPEN).build())
            .withDateChanged(Instant.now().minus(2, ChronoUnit.DAYS))
            .build();

        when(sessionService.findSessionById(sessionId)).thenReturn(Optional.of(currentSession));
        when(studentService.getStudentById(1)).thenReturn(Optional.of(student));
        when(repository.getLastAvailableExam(1, "assessmentId", "SBAC-PT")).thenReturn(Optional.of(previousExam));
        when(sessionService.findSessionById(previousSession.getId())).thenReturn(Optional.of(previousSession));
        ExternalSessionConfiguration externalSessionConfiguration = new ExternalSessionConfiguration("SBAC-PT", "Development", 0, 0);
        when(sessionService.findExternalSessionConfigurationByClientName("SBAC-PT")).thenReturn(Optional.of(externalSessionConfiguration));

        Response<Exam> examResponse = examService.openExam(openExamRequest);

        assertThat(examResponse.getErrors()).isNotPresent();
        assertThat(examResponse.getData()).isPresent();
        assertThat(examResponse.getData().get().getId()).isEqualTo(previousExam.getId());
    }

    @Test
    public void shouldAllowPreviousExamToOpenIfPreviousSessionIsClosed() {
        UUID sessionId = UUID.randomUUID();
        OpenExamRequest openExamRequest = new OpenExamRequest();
        openExamRequest.setStudentId(1);
        openExamRequest.setSessionId(sessionId);
        openExamRequest.setAssessmentId("assessmentId");
        openExamRequest.setClientName("SBAC-PT");
        openExamRequest.setMaxAttempts(5);

        Session currentSession = new Session.Builder()
            .withType(2)
            .build();

        Session previousSession = new Session.Builder()
            .withId(UUID.randomUUID())
            .withType(2)
            .withStatus("closed")
            .build();

        Student student = new Student(1, "testId", "CA", "clientName");

        Exam previousExam = new Exam.Builder()
            .withId(UUID.randomUUID())
            .withSessionId(previousSession.getId())
            .withStatus(new ExamStatusCode.Builder().withStage(ExamStatusCode.STAGE_OPEN).build())
            .withDateChanged(Instant.now())
            .build();

        when(sessionService.findSessionById(sessionId)).thenReturn(Optional.of(currentSession));
        when(studentService.getStudentById(1)).thenReturn(Optional.of(student));
        when(repository.getLastAvailableExam(1, "assessmentId", "SBAC-PT")).thenReturn(Optional.of(previousExam));
        when(sessionService.findSessionById(previousSession.getId())).thenReturn(Optional.of(previousSession));
        ExternalSessionConfiguration externalSessionConfiguration = new ExternalSessionConfiguration("SBAC-PT", "Development", 0, 0);
        when(sessionService.findExternalSessionConfigurationByClientName("SBAC-PT")).thenReturn(Optional.of(externalSessionConfiguration));

        Response<Exam> examResponse = examService.openExam(openExamRequest);

        assertThat(examResponse.getErrors()).isNotPresent();
        assertThat(examResponse.getData()).isPresent();
        assertThat(examResponse.getData().get().getId()).isEqualTo(previousExam.getId());
    }

    @Test
    public void shouldOpenPreviousExamIfSessionIdSame() {
        UUID sessionId = UUID.randomUUID();
        OpenExamRequest openExamRequest = new OpenExamRequest();
        openExamRequest.setStudentId(1);
        openExamRequest.setSessionId(sessionId);
        openExamRequest.setAssessmentId("assessmentId");
        openExamRequest.setClientName("SBAC-PT");
        openExamRequest.setMaxAttempts(5);

        Session currentSession = new Session.Builder()
            .withType(2)
            .build();

        Session previousSession = new Session.Builder()
            .withId(sessionId)
            .withType(2)
            .build();

        Student student = new Student(1, "testId", "CA", "clientName");

        Exam previousExam = new Exam.Builder()
            .withId(UUID.randomUUID())
            .withSessionId(previousSession.getId())
            .withStatus(new ExamStatusCode.Builder().withStage(ExamStatusCode.STAGE_OPEN).build())
            .withDateChanged(Instant.now())
            .build();

        when(sessionService.findSessionById(sessionId)).thenReturn(Optional.of(currentSession));
        when(studentService.getStudentById(1)).thenReturn(Optional.of(student));
        when(repository.getLastAvailableExam(1, "assessmentId", "SBAC-PT")).thenReturn(Optional.of(previousExam));
        when(sessionService.findSessionById(previousSession.getId())).thenReturn(Optional.of(previousSession));
        ExternalSessionConfiguration externalSessionConfiguration = new ExternalSessionConfiguration("SBAC-PT", "Development", 0, 0);
        when(sessionService.findExternalSessionConfigurationByClientName("SBAC-PT")).thenReturn(Optional.of(externalSessionConfiguration));

        Response<Exam> examResponse = examService.openExam(openExamRequest);

        assertThat(examResponse.getErrors()).isNotPresent();
        assertThat(examResponse.getData()).isPresent();
        assertThat(examResponse.getData().get().getId()).isEqualTo(previousExam.getId());
    }

    @Test
    public void shouldOpenPreviousExamIfSessionEndTimeIsBeforeNow() {
        UUID sessionId = UUID.randomUUID();
        OpenExamRequest openExamRequest = new OpenExamRequest();
        openExamRequest.setStudentId(1);
        openExamRequest.setSessionId(sessionId);
        openExamRequest.setAssessmentId("assessmentId");
        openExamRequest.setClientName("SBAC-PT");
        openExamRequest.setMaxAttempts(5);

        Session currentSession = new Session.Builder()
            .withType(2)
            .build();

        Session previousSession = new Session.Builder()
            .withId(UUID.randomUUID())
            .withType(2)
            .withDateEnd(Instant.now().minus(1, ChronoUnit.DAYS))
            .build();

        Student student = new Student(1, "testId", "CA", "clientName");

        Exam previousExam = new Exam.Builder()
            .withId(UUID.randomUUID())
            .withSessionId(previousSession.getId())
            .withStatus(new ExamStatusCode.Builder().withStage(ExamStatusCode.STAGE_OPEN).build())
            .withDateChanged(Instant.now())
            .build();

        when(sessionService.findSessionById(sessionId)).thenReturn(Optional.of(currentSession));
        when(studentService.getStudentById(1)).thenReturn(Optional.of(student));
        when(repository.getLastAvailableExam(1, "assessmentId", "SBAC-PT")).thenReturn(Optional.of(previousExam));
        when(sessionService.findSessionById(previousSession.getId())).thenReturn(Optional.of(previousSession));
        ExternalSessionConfiguration externalSessionConfiguration = new ExternalSessionConfiguration("SBAC-PT", "Development", 0, 0);
        when(sessionService.findExternalSessionConfigurationByClientName("SBAC-PT")).thenReturn(Optional.of(externalSessionConfiguration));

        Response<Exam> examResponse = examService.openExam(openExamRequest);

        assertThat(examResponse.getErrors()).isNotPresent();
        assertThat(examResponse.getData()).isPresent();
        assertThat(examResponse.getData().get().getId()).isEqualTo(previousExam.getId());
    }

    @Test
    public void shouldGetInitialAbilityFromScoresForSameAssessment() {
        final UUID sessionId = UUID.randomUUID();
        final UUID thisExamId = UUID.randomUUID();
        final String assessmentId = "SBAC ELA 3-ELA-3";
        final String clientName = "SBAC_TEST1";
        final long studentId = 9898L;
        final double assessmentAbilityVal = 99D;

        ClientTestProperty clientTestProperty = new ClientTestProperty.Builder()
                .withClientName(clientName)
                .withAssessmentId(assessmentId)
                .withMaxOpportunities(3)
                .withPrefetch(2)
                .withIsSelectable(true)
                .withLabel("Grades 3 - 5 MATH")
                .withSubjectName("ELA")
                .withAccommodationFamily("MATH")
                .withRtsFormField("tds-testform")
                .withRequireRtsWindow(true)
                .withRtsModeField("tds-testmode")
                .withRequireRtsMode(true)
                .withRequireRtsModeWindow(true)
                .withDeleteUnansweredItems(true)
                .withInitialAbilityBySubject(true)
                .withAbilitySlope(1D)
                .withAbilityIntercept(2D)
                .build();

        Exam thisExam = createExam(sessionId, thisExamId, assessmentId, clientName, studentId);

        Ability sameAssessmentAbility = new Ability(
                UUID.randomUUID(), assessmentId, 1, Instant.now(), assessmentAbilityVal);
        Ability differentAssessmentAbility = new Ability(
                UUID.randomUUID(), assessmentId, 1, Instant.now(), 50D);

        List<Ability> abilities = new ArrayList<>();
        abilities.add(sameAssessmentAbility);
        abilities.add(differentAssessmentAbility);
        when(repository.findAbilities(thisExamId, clientName, "ELA", studentId)).thenReturn(abilities);
        Optional<Double> maybeAbilityReturned = examService.getInitialAbility(thisExam, clientTestProperty);

        assertThat(maybeAbilityReturned.get()).isEqualTo(assessmentAbilityVal);
    }

    @Test
    public void shouldGetInitialAbilityFromHistoryWithoutSlopeIntercept() {
        final UUID sessionId = UUID.randomUUID();
        final UUID thisExamId = UUID.randomUUID();
        final String assessmentId = "SBAC ELA 3-ELA-3";
        final String clientName = "SBAC_TEST4";
        final long studentId = 9897L;

        // Null slop/intercept for this test case
        ClientTestProperty clientTestProperty = new ClientTestProperty.Builder()
                .withClientName(clientName)
                .withAssessmentId(assessmentId)
                .withMaxOpportunities(3)
                .withPrefetch(2)
                .withIsSelectable(true)
                .withLabel("Grades 3 - 5 MATH")
                .withSubjectName("ELA")
                .withAccommodationFamily("MATH")
                .withRtsFormField("tds-testform")
                .withRequireRtsWindow(true)
                .withRtsModeField("tds-testmode")
                .withRequireRtsMode(true)
                .withRequireRtsModeWindow(true)
                .withDeleteUnansweredItems(true)
                .withInitialAbilityBySubject(true)
                .build();

        Exam thisExam = createExam(sessionId, thisExamId, assessmentId, clientName, studentId);
        List<Ability> abilities = new ArrayList<>();
        Optional<Double> maybeAbility = Optional.of(66D);
        when(repository.findAbilities(thisExamId, clientName, "ELA", studentId)).thenReturn(abilities);
        when(historyRepository.findAbilityFromHistoryForSubjectAndStudent(clientName, "ELA", studentId))
                .thenReturn(maybeAbility);
        Optional<Double> maybeAbilityReturned = examService.getInitialAbility(thisExam, clientTestProperty);
        assertThat(maybeAbilityReturned.get()).isEqualTo(maybeAbility.get());
    }

    @Test
    public void shouldGetNullInitialAbility() {
        final UUID sessionId = UUID.randomUUID();
        final UUID thisExamId = UUID.randomUUID();
        final String assessmentId = "SBAC ELA 3-ELA-3";
        final String clientName = "SBAC_TEST7";
        final long studentId = 9898L;
        final double assessmentAbilityVal = 99F;
        final Double slope = 2D;
        final Double intercept = 1D;

        ClientTestProperty clientTestProperty = new ClientTestProperty.Builder()
                .withClientName(clientName)
                .withAssessmentId(assessmentId)
                .withMaxOpportunities(3)
                .withPrefetch(2)
                .withIsSelectable(true)
                .withLabel("Grades 3 - 5 MATH")
                .withSubjectName("ELA")
                .withAccommodationFamily("MATH")
                .withRtsFormField("tds-testform")
                .withRequireRtsWindow(true)
                .withRtsModeField("tds-testmode")
                .withRequireRtsMode(true)
                .withRequireRtsModeWindow(true)
                .withDeleteUnansweredItems(true)
                .withInitialAbilityBySubject(true)
                .withAbilitySlope(slope)
                .withAbilityIntercept(intercept)
                .build();

        Exam thisExam = createExam(sessionId, thisExamId, assessmentId, clientName, studentId);

        List<Ability> abilities = new ArrayList<>();
        when(repository.findAbilities(thisExamId, clientName, "ELA", studentId)).thenReturn(abilities);
        when(historyRepository.findAbilityFromHistoryForSubjectAndStudent(clientName, "ELA", studentId))
                .thenReturn(Optional.empty());
        when(assessmentService.findSetOfAdminSubjectByKey(thisExam.getAssessmentId())).thenReturn(Optional.empty());
        Optional<Double> maybeAbilityReturned = examService.getInitialAbility(thisExam, clientTestProperty);
        assertThat(maybeAbilityReturned).isNotPresent();
    }

    @Test
    public void shouldGetInitialAbilityFromItembank() {
        final UUID sessionId = UUID.randomUUID();
        final UUID thisExamId = UUID.randomUUID();
        final String assessmentId = "SBAC ELA 3-ELA-3";
        final String clientName = "SBAC_TEST6";
        final long studentId = 9898L;
        final float assessmentAbilityVal = 99F;
        final Double slope = 2D;
        final Double intercept = 1D;

        SetOfAdminSubject setOfAdminSubject = new SetOfAdminSubject(
                "(SBAC)SBAC ELA 3-ELA-3-Spring-2112a",
                assessmentId,
                false,
                "jeff-j-sort",
                assessmentAbilityVal
        );

        ClientTestProperty clientTestProperty = new ClientTestProperty.Builder()
                .withClientName(clientName)
                .withAssessmentId(assessmentId)
                .withMaxOpportunities(3)
                .withPrefetch(2)
                .withIsSelectable(true)
                .withLabel("Grades 3 - 5 MATH")
                .withSubjectName("ELA")
                .withAccommodationFamily("MATH")
                .withRtsFormField("tds-testform")
                .withRequireRtsWindow(true)
                .withRtsModeField("tds-testmode")
                .withRequireRtsMode(true)
                .withRequireRtsModeWindow(true)
                .withDeleteUnansweredItems(true)
                .withInitialAbilityBySubject(true)
                .withAbilitySlope(slope)
                .withAbilityIntercept(intercept)
                .build();

        Exam thisExam = createExam(sessionId, thisExamId, assessmentId, clientName, studentId);

        List<Ability> abilities = new ArrayList<>();
        when(repository.findAbilities(thisExamId, clientName, "ELA", studentId)).thenReturn(abilities);
        when(historyRepository.findAbilityFromHistoryForSubjectAndStudent(clientName, "ELA", studentId))
                .thenReturn(Optional.empty());
        when(assessmentService.findSetOfAdminSubjectByKey(thisExam.getAssessmentId())).thenReturn(Optional.of(setOfAdminSubject));
        Optional<Double> maybeAbilityReturned = examService.getInitialAbility(thisExam, clientTestProperty);
        assertThat(maybeAbilityReturned.get()).isEqualTo(assessmentAbilityVal);
    }

    @Test
    public void shouldGetInitialAbilityFromHistoryWithSlopeIntercept() {
        final UUID sessionId = UUID.randomUUID();
        final UUID thisExamId = UUID.randomUUID();
        final String assessmentId = "SBAC ELA 3-ELA-3";
        final String clientName = "SBAC_TEST3";
        final long studentId = 9898L;
        final Double slope = 2D;
        final Double intercept = 1D;

        ClientTestProperty clientTestProperty = new ClientTestProperty.Builder()
                .withClientName(clientName)
                .withAssessmentId(assessmentId)
                .withMaxOpportunities(3)
                .withPrefetch(2)
                .withIsSelectable(true)
                .withLabel("Grades 3 - 5 MATH")
                .withSubjectName("ELA")
                .withAccommodationFamily("MATH")
                .withRtsFormField("tds-testform")
                .withRequireRtsWindow(true)
                .withRtsModeField("tds-testmode")
                .withRequireRtsMode(true)
                .withRequireRtsModeWindow(true)
                .withDeleteUnansweredItems(true)
                .withInitialAbilityBySubject(true)
                .withAbilitySlope(slope)
                .withAbilityIntercept(intercept)
                .build();

        Exam thisExam = createExam(sessionId, thisExamId, assessmentId, clientName, studentId);
        List<Ability> abilities = new ArrayList<>();
        Optional<Double> maybeAbility = Optional.of(66D);
        when(repository.findAbilities(thisExamId, clientName, "ELA", studentId)).thenReturn(abilities);
        when(historyRepository.findAbilityFromHistoryForSubjectAndStudent(clientName, "ELA", studentId))
                .thenReturn(maybeAbility);
        Optional<Double> maybeAbilityReturned = examService.getInitialAbility(thisExam, clientTestProperty);
        // y=mx+b
        double abilityCalulated = maybeAbility.get() * slope + intercept;
        assertThat(maybeAbilityReturned.get()).isEqualTo((float)abilityCalulated);
    }

    @Test
    public void shouldGetInitialAbilityFromScoresForDifferentAssessment() {
        final UUID sessionId = UUID.randomUUID();
        final UUID thisExamId = UUID.randomUUID();
        final String assessmentId = "SBAC ELA 3-ELA-3";
        final String clientName = "SBAC_TEST2";
        final long studentId = 9899L;
        final double assessmentAbilityVal = 75D;

        ClientTestProperty clientTestProperty = new ClientTestProperty.Builder()
                .withClientName(clientName)
                .withAssessmentId(assessmentId)
                .withMaxOpportunities(3)
                .withPrefetch(2)
                .withIsSelectable(true)
                .withLabel("Grades 3 - 5 MATH")
                .withSubjectName("ELA")
                .withAccommodationFamily("MATH")
                .withRtsFormField("tds-testform")
                .withRequireRtsWindow(true)
                .withRtsModeField("tds-testmode")
                .withRequireRtsMode(true)
                .withRequireRtsModeWindow(true)
                .withDeleteUnansweredItems(true)
                .withInitialAbilityBySubject(true)
                .withAbilitySlope(1D)
                .withAbilityIntercept(2D)
                .build();

        Exam thisExam = createExam(sessionId, thisExamId, assessmentId, clientName, studentId);

        Ability sameAssessmentAbility = new Ability(
                UUID.randomUUID(), "assessmentid-2", 1, Instant.now(), assessmentAbilityVal);
        Ability differentAssessmentAbility = new Ability(
                UUID.randomUUID(), "assessmentid-2", 1, Instant.now(), 50D);

        List<Ability> abilities = new ArrayList<>();
        abilities.add(sameAssessmentAbility);
        abilities.add(differentAssessmentAbility);
        when(repository.findAbilities(thisExamId, clientName, "ELA", studentId)).thenReturn(abilities);
        Optional<Double> maybeAbilityReturned = examService.getInitialAbility(thisExam, clientTestProperty);
        assertThat(maybeAbilityReturned.get()).isEqualTo(assessmentAbilityVal);
    }

    @Test
    public void shouldOpenPreviousExamIfPreviousExamIsInactiveStage() {
        UUID sessionId = UUID.randomUUID();
        OpenExamRequest openExamRequest = new OpenExamRequest();
        openExamRequest.setStudentId(-1);
        openExamRequest.setSessionId(sessionId);
        openExamRequest.setAssessmentId("assessmentId");
        openExamRequest.setClientName("SBAC-PT");
        openExamRequest.setMaxAttempts(5);

        Session currentSession = new Session.Builder()
            .withType(2)
            .build();

        Session previousSession = new Session.Builder()
            .withId(UUID.randomUUID())
            .withType(2)
            .withDateEnd(Instant.now().minus(1, ChronoUnit.DAYS))
            .build();

        Exam previousExam = new Exam.Builder()
            .withId(UUID.randomUUID())
            .withSessionId(previousSession.getId())
            .withStatus(new ExamStatusCode.Builder().withStage(ExamStatusCode.STAGE_INACTIVE).build())
            .withDateChanged(Instant.now())
            .build();

        when(sessionService.findSessionById(sessionId)).thenReturn(Optional.of(currentSession));
        when(repository.getLastAvailableExam(-1, "assessmentId", "SBAC-PT")).thenReturn(Optional.of(previousExam));
        when(sessionService.findSessionById(previousSession.getId())).thenReturn(Optional.of(previousSession));
        ExternalSessionConfiguration externalSessionConfiguration = new ExternalSessionConfiguration("SBAC-PT", "Development", 0, 0);
        when(sessionService.findExternalSessionConfigurationByClientName("SBAC-PT")).thenReturn(Optional.of(externalSessionConfiguration));

        Response<Exam> examResponse = examService.openExam(openExamRequest);

        assertThat(examResponse.getErrors()).isNotPresent();
        assertThat(examResponse.getData()).isPresent();
        assertThat(examResponse.getData().get().getId()).isEqualTo(previousExam.getId());
    }


    private Exam createExam(UUID sessionId, UUID thisExamId, String assessmentId, String clientName, long studentId) {
        return new Exam.Builder()
                .withId(thisExamId)
                .withClientName(clientName)
                .withSessionId(sessionId)
                .withAssessmentId(assessmentId)
                .withSubject("ELA")
                .withStudentId(studentId)
                .withStatus(new ExamStatusCode.Builder().withStage(ExamStatusCode.STAGE_OPEN).build())
                .withDateChanged(Instant.now())
                .withDateScored(Instant.now())
                .build();
    }
}