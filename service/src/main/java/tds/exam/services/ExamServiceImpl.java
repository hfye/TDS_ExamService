package tds.exam.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import tds.exam.Exam;
import tds.exam.repository.ExamRepository;

import java.util.UUID;

@Service
public class ExamServiceImpl implements ExamService {
    private final ExamRepository examRepository;

    @Autowired
    public ExamServiceImpl(ExamRepository examRepository) {
        this.examRepository = examRepository;
    }

    @Override
    public Exam getExam(UUID id) {
        return examRepository.getExamById(id);
    }
}
