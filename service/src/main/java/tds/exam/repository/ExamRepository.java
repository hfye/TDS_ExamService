package tds.exam.repository;

import tds.exam.Exam;

import java.util.UUID;

public interface ExamRepository {
    Exam getExamById(UUID id);
}
