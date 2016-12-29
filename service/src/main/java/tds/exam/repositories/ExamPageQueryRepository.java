package tds.exam.repositories;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import tds.exam.models.ExamPage;

/**
 * Handles data reads from the exam_page tables
 */
public interface ExamPageQueryRepository {
    /**
     * Fetches all non-deleted {@link tds.exam.models.ExamPage}s for an exam
     *
     * @param examId the id of the {@link tds.exam.Exam} that the {@link tds.exam.models.ExamPage}s correspond to
     * @return
     */
    List<ExamPage> findAllPages(UUID examId);
}
