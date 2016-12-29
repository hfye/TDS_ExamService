package tds.exam.repositories;

import java.util.List;

import tds.exam.models.ExamResponse;

/**
 * Handles data modification to the exam_item_response tables
 */
public interface ExamResponseCommandRepository {
    /**
     * Inserts a collection of {@link tds.exam.models.ExamResponse}s
     *
     * @param examResponses the item responses to insert
     */
    void insert(List<ExamResponse> examResponses);
}
