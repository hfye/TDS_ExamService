package tds.exam.services;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import tds.assessment.ItemConstraint;
import tds.assessment.Segment;
import tds.exam.Exam;
import tds.exam.models.SegmentPoolInfo;

/**
 * Created by emunoz on 11/7/16.
 */
public interface SegmentPoolService {
    /**
     *
     *
     * @param examId        The id of the {@link Exam}
     * @param segment       The segment being constructed
     * @return      The {@link tds.exam.models.SegmentPoolInfo} containing segment pool information
     */
    SegmentPoolInfo computeSegmentPool(UUID examId, Segment segment, List<ItemConstraint> itemConstraints);
}
