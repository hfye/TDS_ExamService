package tds.exam.services;

import tds.exam.Exam;

import java.util.UUID;

public interface ExamService {
    Exam getExam(UUID uuid);
}
