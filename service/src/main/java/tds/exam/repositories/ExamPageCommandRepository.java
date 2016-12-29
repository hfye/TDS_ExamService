package tds.exam.repositories;

import java.util.List;
import java.util.UUID;

import tds.exam.models.ExamPage;

/**
 * Handles data modification for the exam_page table
 */
public interface ExamPageCommandRepository {
    /**
     * Inserts a collection of {@link tds.exam.models.ExamPage}s
     *
     * @param examPages
     */
    void insert(List<ExamPage> examPages);

    /**
     * Marks all {@link tds.exam.models.ExamPage}s for the exam as deleted
     *
     * @param examId the id of the {@link tds.exam.Exam}
     */
    void delete(UUID examId);
}
