package tds.exam.services;

import java.util.List;
import java.util.Optional;

import tds.assessment.Assessment;
import tds.assessment.ItemConstraint;
import tds.assessment.ItemProperty;

/**
 * Service handles assessment interaction
 */
public interface AssessmentService {
    /**
     * Finds the {@link tds.assessment.Assessment}
     * @param key unique key for the assessment
     * @return {@link tds.assessment.Assessment the assessment}
     */
    Optional<Assessment> findAssessmentByKey(String key);

    /**
     * Finds the list of {@link tds.assessment.ItemConstraint} for the {@link tds.assessment.Assessment}
     *
     * @param clientName    The environment clientname
     * @param assessmentId  The assessment id of the item constraints
     * @return  the list of matching item constraints
     */
    List<ItemConstraint> findItemConstraints(String clientName, String assessmentId);

    /**
     * Finds the list of {@link tds.assessment.ItemProperty} for the {@link tds.assessment.Segment}
     *
     * @param segmentKey the segment
     * @return
     */
    List<ItemProperty> findActiveItemProperties(String segmentKey);
}
