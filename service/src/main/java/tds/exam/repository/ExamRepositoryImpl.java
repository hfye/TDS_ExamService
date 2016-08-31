package tds.exam.repository;

import org.springframework.stereotype.Repository;
import tds.exam.Exam;

import java.util.UUID;

@Repository
public class ExamRepositoryImpl implements ExamRepository {

    @Override
    public Exam getExamById(UUID id) {
        return new Exam(id);
    }
}
