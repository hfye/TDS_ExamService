package tds.exam.services.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

import tds.exam.models.ExamPage;
import tds.exam.repositories.ExamPageCommandRepository;
import tds.exam.repositories.ExamPageQueryRepository;
import tds.exam.repositories.ExamResponseQueryRepository;
import tds.exam.services.ExamItemService;

/**
 * A service for handling interactions with exam items, pages, and responses.
 */
@Service
public class ExamItemServiceImpl implements ExamItemService {
    private final ExamPageCommandRepository examPageCommandRepository;
    private final ExamPageQueryRepository examPageQueryRepository;
    private final ExamResponseQueryRepository examResponseQueryRepository;

    @Autowired
    public ExamItemServiceImpl(ExamPageQueryRepository examPageQueryRepository,
                               ExamPageCommandRepository examPageCommandRepository,
                               ExamResponseQueryRepository examResponseQueryRepository)
    {
        this.examPageCommandRepository = examPageCommandRepository;
        this.examPageQueryRepository = examPageQueryRepository;
        this.examResponseQueryRepository = examResponseQueryRepository;
    }

    @Override
    public void insertPages(final List<ExamPage> examPages) {
        examPageCommandRepository.insert(examPages);
    }

    @Override
    public void deletePages(final UUID examId) {
        examPageCommandRepository.deleteAll(examId);
    }

    @Override
    public int getExamPosition(final UUID examId) {
        return examResponseQueryRepository.getCurrentExamItemPosition(examId);
    }

    @Override
    public List<ExamPage> findAllPages(final UUID examId) {
        return examPageQueryRepository.findAll(examId);
    }
}
