package tds.exam.repositories;

import java.util.List;

import tds.exam.models.ExamItemResponse;

/**
 * Handles data modification to the exam_item_response tables
 */
public interface ExamResponseCommandRepository {
    /**
     * Inserts a collection of {@link tds.exam.models.ExamItemResponse}s
     *
     * @param examItemResponses the item responses to insert
     */
    void insert(List<ExamItemResponse> examItemResponses);
}
