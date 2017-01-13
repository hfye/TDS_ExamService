package tds.exam.services;

import java.util.List;
import java.util.Set;

import tds.assessment.Assessment;
import tds.exam.Exam;
import tds.exam.models.FieldTestItemGroup;

/**
 * An service for selecting field test item groups
 */
public interface FieldTestItemGroupSelector {
    /**
     * This method returns a list
     *
     * @param exam the {@link tds.exam.Exam} to select {@link tds.exam.models.FieldTestItemGroup}s for
     * @param assignedGroupIds a {@link java.util.Set} of group ids that have already been assigned for this {@link tds.exam.Exam}.
     * @param assessment the {@link tds.assessment.Assessment} to select {@link tds.exam.models.FieldTestItemGroup}s for
     * @param segmentKey the key of the {@link tds.assessment.Segment} to select {@link tds.exam.models.FieldTestItemGroup}s for
     * @param numItems the number of items to select
     * @return
     */
    List<FieldTestItemGroup> selectItemGroupsLeastUsed(Exam exam, Set<String> assignedGroupIds,
                                                       Assessment assessment, String segmentKey, int numItems);
}
