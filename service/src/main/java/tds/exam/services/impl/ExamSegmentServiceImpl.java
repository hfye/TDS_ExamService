package tds.exam.services.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import tds.assessment.Algorithm;
import tds.assessment.Assessment;
import tds.assessment.Segment;
import tds.exam.ExamAccommodation;
import tds.exam.Exam;
import tds.exam.models.ExamSegment;
import tds.exam.models.FormInfo;
import tds.exam.models.SegmentPoolInfo;
import tds.exam.repositories.ExamAccommodationQueryRepository;
import tds.exam.repositories.ExamSegmentCommandRepository;
import tds.exam.repositories.ExamSegmentQueryRepository;
import tds.exam.services.ExamSegmentService;
import tds.exam.services.SegmentPoolService;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

//@Service
public class ExamSegmentServiceImpl implements ExamSegmentService {
    private static final Logger LOG = LoggerFactory.getLogger(ExamSegmentServiceImpl.class);
    private static final String LANGUAGE_ACC_TYPE = "Language";

    private ExamSegmentQueryRepository queryRepository;
    private ExamSegmentCommandRepository commandRepository;
    private ExamAccommodationQueryRepository examAccommodationQueryRepository;
    private SegmentPoolService segmentPoolService;

    @Autowired
    public ExamSegmentServiceImpl (ExamSegmentQueryRepository queryRepository,
                                   ExamSegmentCommandRepository commandRepository,
                                   ExamAccommodationQueryRepository examAccommodationQueryRepository,
                                   SegmentPoolService segmentPoolService) {
        this.queryRepository = queryRepository;
        this.commandRepository = commandRepository;
        this.examAccommodationQueryRepository = examAccommodationQueryRepository;
        this.segmentPoolService = segmentPoolService;
    }

    /**
     * @inheritDoc
     */
    @Override
    public List<String> initializeExamSegments(Exam exam, Assessment assessment, List<String> formKeys) {
        List<String> formIds = new ArrayList<>();

        // Are there already exam segments created for this exam? TODO: check if this is even possible/necessary
        // StudentDLL 4540
        if (!queryRepository.findByExamId(exam.getId()).isEmpty()) {
            LOG.debug(String.format("Exam segments already configured for examId {}. Aborting InitializeExamSegments...",
                    exam.getId()));
            return formIds;
        }

        // StudentDLL 4589
        List<ExamAccommodation> languageAccommodation = examAccommodationQueryRepository.findAccommodations(exam.getId(),
                assessment.getSegments().get(0).getKey(), new String[] { LANGUAGE_ACC_TYPE });
        final String languageCode = languageAccommodation.get(0).getCode();
        Optional<String> maybeFormCohort = Optional.empty();

        // StudentDLL 4623 - 4636
        List<ExamSegment> initializedExamSegments = initializeSegments(exam.getId(), assessment);
        List<ExamSegment> examSegmentsToInsert = new ArrayList<>();
        boolean itemsInPool = false;

        // StudentDLL 4651
        // For each segment, keep track of segmentPosition (1-based)
        for (ExamSegment examSegment : initializedExamSegments) {
            boolean isSatisfied = false;
            Set<String> items = new HashSet<>();
            int fieldTestItemCount = 0;
            int itemPoolCount;
            // This flag is to ensure that the resulting ExamSegments contain more than zero items.\

            /* NOTE: Skipping 4660-4678. This segment of code just increments the position and re-iterates to the next
                segment. This is done in case the minimumSegmentPosition is not found in the temp table they create.
                That can never happen with our data structure, as it is always 1-based segment positioning.
             */
            if (examSegment.getAlgorithm().equals(Algorithm.FIXED_FORM.getType())) {
                FormInfo formInfo = selectTestForm(exam.getId(), assessment.getKey(), languageCode, formKeys, maybeFormCohort);

                if (formInfo.getFormLength() == null) { //TODO: should this be == 0?
                    //TODO: Error - throw exception
//                    throw new EmptyExamFormException("Unable to complete test form selection");
                }
                itemPoolCount = formInfo.getFormLength();

                if (!maybeFormCohort.isPresent()) {
                    maybeFormCohort = getFormCohort(examSegment, assessment);
                }
            } else { // Algorithm is adaptive2
                SegmentPoolInfo segmentPoolInfo = segmentPoolService.computeSegmentPool(exam.getId(),
                        assessment.getSegment(examSegment.getAssessmentSegmentKey()), assessment.getItemConstraints());
                boolean isEligible = true; //TODO: FT_IsEligible_FN
                fieldTestItemCount = 0;
                items = segmentPoolInfo.getItemPoolIds();
                itemPoolCount = segmentPoolInfo.getItemPoolCount();

                if (isEligible && segmentPoolInfo.getLength() == examSegment.getExamItemCount()) {
//                     TODO: implement FT_SelectItemGroups
//                    fieldTestItemCount = selectFieldTestItemGroups(exam.getId(), assessment.getKey(),
//                              examSegment.getSegmentPosition(), examSegment.getAssessmentSegmentId());
                }

                if (fieldTestItemCount + segmentPoolInfo.getLength() == 0) {
                    isSatisfied = true;
                }
            }

            itemsInPool = (itemPoolCount > 0 || fieldTestItemCount > 0);
            // "Update" the exam segment with the new segment information - we'll have to create a new copy since ExamSegment is immutable.
            //TODO: uncomment when implemented
//            examSegmentsToInsert.add(updateExamSegment(examSegment, items, itemPoolCount, formLength, newLength, cohort,
//                    formKey, formId, fieldTestItemCount, isSatisfied);
        }

        if (!itemsInPool) {
            //TODO: Error - throw exception
//            throw new EmptySegmentPoolException("There are no items in the segment pool.");
        }

        // Insert to exam segments table TODO: batch?
        examSegmentsToInsert.forEach((item) -> {
            commandRepository.insert(item);
        });

        return formIds;
    }

    private FormInfo selectTestForm(UUID id, String key, String languageCode, List<String> formKeys, Optional<String> maybeFormCohort) {
        //TODO: Implement this either in its own class or here
        return null;
    }

    private ExamSegment updateExamSegment(ExamSegment segment, List<String> itemPool, Integer poolCount, Integer formLength, Integer newLength,
                                          String cohort, String formKey, String formId, Integer fieldTestCount, boolean isSatisfied) {
        return new ExamSegment.Builder()
                .withExamId(segment.getExamId())
                .withAssessmentSegmentId(segment.getAssessmentSegmentId())
                .withAssessmentSegmentKey(segment.getAssessmentSegmentKey())
                .withSegmentPosition(segment.getSegmentPosition())
                .withAlgorithm(segment.getAlgorithm())
                .withExamItemCount(segment.getExamItemCount())
                .withIsPermeable(segment.isPermeable())
                .withIsSatisfied(isSatisfied)
                .withFormId(formId)
                .withFormKey(formKey)
                .withFieldTestItemCount(fieldTestCount)
                .withPoolCount(poolCount)
                .withFormCohort(cohort)
                .withExamItemCount(segment.getAlgorithm().equalsIgnoreCase("fixedform") ? formLength : newLength)
                .withItemPool(itemPool)
                .build();
    }

    private Optional<String> getFormCohort(ExamSegment examSegment, Assessment assessment) {
        Optional<String> maybeCohort = Optional.empty();
        Optional<Segment> maybeSegment = assessment.getSegments().stream().filter(
                seg -> seg.getKey().equals(examSegment.getAssessmentSegmentKey())
        ).findFirst();

        if (maybeSegment.isPresent()) {
            // Get the first segment's cohort
            maybeCohort = Optional.of(maybeSegment.get().getForms().get(0).getCohort());
        }

        return maybeCohort;
    }

    private List<ExamSegment> initializeSegments(UUID examId, Assessment assessment) {
        List<ExamSegment> examSegmentsInitialized = new ArrayList<>();

        if (assessment.isSegmented()) {
            examSegmentsInitialized.addAll(assessment.getSegments().stream().map(assessmentSegment -> new ExamSegment.Builder()
                    .withExamId(examId)
                    .withAssessmentSegmentKey(assessmentSegment.getKey())
                    .withAssessmentSegmentId(assessmentSegment.getSegmentId())
                    .withSegmentPosition(assessmentSegment.getPosition())
                    .withAlgorithm(assessmentSegment.getSelectionAlgorithm().getType())
                    .withExamItemCount(assessment.getSegments().get(0).getMaxItems())
                    .withIsPermeable(false)
                    .withIsSatisfied(false)
                    .build()).collect(Collectors.toList())
            );
        } else {
            examSegmentsInitialized.add(new ExamSegment.Builder()
                .withExamId(examId)
                .withAssessmentSegmentKey(assessment.getKey())
                .withAssessmentSegmentId(assessment.getAssessmentId())
                .withSegmentPosition(assessment.getSegments().get(0).getPosition())
                .withAlgorithm(assessment.getSelectionAlgorithm().toString())
                .withExamItemCount(assessment.getSegments().get(0).getMaxItems())
                .withIsPermeable(false)
                .withIsSatisfied(false)
                .build()
            );
        }

        return examSegmentsInitialized;
    }


}
