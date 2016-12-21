package tds.exam.utils;


import org.junit.Test;

import java.util.UUID;

import tds.assessment.Assessment;
import tds.config.TimeLimitConfiguration;
import tds.exam.ExamConfiguration;
import tds.exam.ExamStatusCode;
import tds.exam.builder.AssessmentBuilder;

import static org.assertj.core.api.Assertions.assertThat;

public class ExamConfigurationHelperTest {
    @Test
    public void shouldReturnNewExamConfiguration() {
        final UUID examId = UUID.randomUUID();
        Assessment assessment = new AssessmentBuilder().build();
        TimeLimitConfiguration timeLimitConfiguration = new TimeLimitConfiguration.Builder()
            .withTaCheckinTimeMinutes(3)
            .withAssessmentId(assessment.getAssessmentId())
            .withExamDelayDays(2)
            .withExamRestartWindowMinutes(2)
            .withInterfaceTimeoutMinutes(4)
            .withRequestInterfaceTimeoutMinutes(5)
            .build();
        final int testLength = 5;
        ExamConfiguration examConfiguration = ExamConfigurationHelper.getNew(examId, assessment, timeLimitConfiguration,
            testLength);

        assertThat(examConfiguration).isNotNull();
        assertThat(examConfiguration.getExamId()).isEqualTo(examId);
        assertThat(examConfiguration.getContentLoadTimeoutMinutes()).isEqualTo(120);
        assertThat(examConfiguration.getInterfaceTimeoutMinutes()).isEqualTo(timeLimitConfiguration.getInterfaceTimeoutMinutes());
        assertThat(examConfiguration.getTestLength()).isEqualTo(testLength);
        assertThat(examConfiguration.getStartPosition()).isEqualTo(1);
        assertThat(examConfiguration.getAttempt()).isEqualTo(0);
        assertThat(examConfiguration.getExamRestartWindowMinutes()).isEqualTo(timeLimitConfiguration.getExamRestartWindowMinutes());
        assertThat(examConfiguration.getFailureMessage()).isNull();
        assertThat(examConfiguration.getRequestInterfaceTimeoutMinutes()).isEqualTo(timeLimitConfiguration.getRequestInterfaceTimeoutMinutes());
        assertThat(examConfiguration.getStatus()).isEqualTo(ExamStatusCode.STATUS_STARTED);
        assertThat(examConfiguration.getPrefetch()).isEqualTo(assessment.getPrefetch());
    }
}
