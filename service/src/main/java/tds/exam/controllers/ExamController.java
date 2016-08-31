package tds.exam.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import tds.exam.Exam;
import tds.exam.services.ExamService;

import java.util.UUID;

@RestController
public class ExamController {
    private final ExamService examService;

    @Autowired
    public ExamController(ExamService examService) {
        this.examService = examService;
    }

    @RequestMapping("/exam/{id}")
    public Exam getExamById(@PathVariable UUID id) {
        return examService.getExam(id);
    }
}
