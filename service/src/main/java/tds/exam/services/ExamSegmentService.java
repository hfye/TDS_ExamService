package tds.exam.services;

import tds.assessment.Assessment;
import tds.exam.Exam;
import tds.exam.models.ExamSegment;

import java.util.List;

/**
 * Service that handles interactions with exam segments.
 */
public interface ExamSegmentService {

    /**
     * Initializes the {@link ExamSegment}s for the {@link Exam}.
     *
     * @param exam    The {@link Exam} to initialize segments for
     * @return        A list of formId references for the {@link Exam}
     */
    List<String> initializeExamSegments(Exam exam, Assessment assessment, List<String> formKeys);
}
