package tds.exam.utils;

import java.util.UUID;

import tds.assessment.Assessment;
import tds.config.TimeLimitConfiguration;
import tds.exam.ExamConfiguration;
import tds.exam.ExamStatusCode;

/**
 * A utility class for creating {@link tds.exam.ExamConfiguration} objects.
 */
public class ExamConfigurationHelper {
    private static final int CONTENT_LOAD_TIMEOUT = 120;

    /*
        This method mimics the legacy TestConfigHelper.getNew().
        - scoreByTds has been removed as it is unused by the application.
     */
    public static ExamConfiguration getNew(UUID examId, Assessment assessment, TimeLimitConfiguration timeLimitConfiguration,
                                           int testLength) {
        return new ExamConfiguration.Builder()
            .withExamId(examId)
            .withContentLoadTimeout(CONTENT_LOAD_TIMEOUT)
            .withInterfaceTimeout(timeLimitConfiguration.getInterfaceTimeoutMinutes())
            .withExamRestartWindowMinutes(timeLimitConfiguration.getExamRestartWindowMinutes())
            .withPrefetch(assessment.getPrefetch())
            .withValidateCompleteness(assessment.isValidateCompleteness())
            .withRequestInterfaceTimeout(timeLimitConfiguration.getRequestInterfaceTimeoutMinutes())
            .withAttempt(0)
            .withStartPosition(1)
            .withStatus(ExamStatusCode.STATUS_STARTED)
            .withTestLength(testLength)
            .build();
    }
}
