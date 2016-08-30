package tds.exam;

import org.junit.Test;
import static org.assertj.core.api.Assertions.*;
import java.util.UUID;

public class ExamTest {
    @Test
    public void anExamCanBeCreated() {
        UUID examId = UUID.randomUUID();
        Exam exam = new Exam(examId);

        assertThat(exam.getId()).isEqualTo(examId);
    }
}
