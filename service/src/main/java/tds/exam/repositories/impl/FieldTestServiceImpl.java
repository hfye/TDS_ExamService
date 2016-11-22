package tds.exam.repositories.impl;

import org.springframework.beans.factory.annotation.Autowired;

import java.util.Optional;

import tds.assessment.Assessment;
import tds.assessment.Segment;
import tds.config.ClientTestProperty;
import tds.exam.Exam;
import tds.exam.models.ExamSegment;
import tds.exam.services.AssessmentService;
import tds.exam.services.ConfigService;
import tds.exam.services.FieldTestService;

/**
 * Created by emunoz on 11/6/16.
 */
public class FieldTestServiceImpl implements FieldTestService {

    private AssessmentService assessmentService;
    private ConfigService configService;

    @Autowired
    public FieldTestServiceImpl(AssessmentService assessmentService) {
        this.assessmentService = assessmentService;
    }

    /**
     * @inheritdoc
     */
    @Override
    public boolean isFieldTestEligible(Exam exam, Assessment assessment, String segmentKey, String language,
                                       String environment) {
        boolean isEligible = false;

        //TODO itembank fieldtest check
        int fieldTestItems = 0; //assessmentService.containsFieldTestItems(segment.getAssessmentSegmentKey(), language);


        if (fieldTestItems > 0) {
            Optional<Segment> currentSegment = assessment.getSegments().stream().filter(
                    segment -> segment.getKey().equals(segmentKey)).findAny();

            if (!currentSegment.isPresent()) {
                throw new IllegalArgumentException(
                        String.format("No segment with key {} found in Assessment {}", segmentKey, assessment.getKey()));
            }
            // Check 1 passed
            if (currentSegment.get().getFieldTestMinItems() > 0) {

            } else if ("SIMULATION".equals(environment)) {
//                Optional<ClientTestProperty> property = configService.findClientTestProperty(exam.getClientName(),
//                        assessment.getAssessmentId());
//
//                if (property.isPresent()) {
//
//                }
            }
        }

        return isEligible;
    }

}
