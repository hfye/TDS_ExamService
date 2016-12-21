package tds.exam;

import java.util.UUID;

/**
 * Class containing exam configuration data
 */
public class ExamConfiguration {
    private UUID examId;
    private int contentLoadTimeout;
    private int interfaceTimeout;
    private int examRestartWindowMinutes;
    private int prefetch;
    private int requestInterfaceTimeout;
    private int attempt;
    private int startPosition;
    private String status;
    private String failureMessage;
    private int testLength;
    private boolean validateCompleteness;

    private ExamConfiguration() {}

    private ExamConfiguration(Builder builder) {
        this.examId = builder.examId;
        this.contentLoadTimeout = builder.contentLoadTimeout;
        this.interfaceTimeout = builder.interfaceTimeout;
        this.examRestartWindowMinutes = builder.examRestartWindowMinutes;
        this.prefetch = builder.prefetch;
        this.requestInterfaceTimeout = builder.requestInterfaceTimeout;
        this.attempt = builder.attempt;
        this.startPosition = builder.startPosition;
        this.status = builder.status;
        this.failureMessage = builder.failureMessage;
        this.testLength = builder.testLength;
        this.validateCompleteness = builder.validateCompleteness;
    }

    public static class Builder {
        private UUID examId;
        private int contentLoadTimeout;
        private int interfaceTimeout;
        private int examRestartWindowMinutes;
        private int prefetch;
        private int requestInterfaceTimeout;
        private int attempt;
        private int startPosition;
        private String status;
        private String failureMessage;
        private int testLength;
        private boolean validateCompleteness;

        public Builder withExamId(UUID examId) {
            this.examId = examId;
            return this;
        }

        public Builder withContentLoadTimeout(int contentLoadTimeout) {
            this.contentLoadTimeout = contentLoadTimeout;
            return this;
        }

        public Builder withInterfaceTimeout(int interfaceTimeout) {
            this.interfaceTimeout = interfaceTimeout;
            return this;
        }

        public Builder withExamRestartWindowMinutes(int examRestartWindowMinutes) {
            this.examRestartWindowMinutes = examRestartWindowMinutes;
            return this;
        }

        public Builder withPrefetch(int prefetch) {
            this.prefetch = prefetch;
            return this;
        }

        public Builder withRequestInterfaceTimeout(int requestInterfaceTimeout) {
            this.requestInterfaceTimeout = requestInterfaceTimeout;
            return this;
        }

        public Builder withAttempt(int attempt) {
            this.attempt = attempt;
            return this;
        }

        public Builder withStartPosition(int startPosition) {
            this.startPosition = startPosition;
            return this;
        }

        public Builder withStatus(String status) {
            this.status = status;
            return this;
        }

        public Builder withTestLength(int testLength) {
            this.testLength = testLength;
            return this;
        }

        public Builder withValidateCompleteness(boolean validateCompleteness) {
            this.validateCompleteness = validateCompleteness;
            return this;
        }

        public Builder withFailureMessage(String failureMessage) {
            this.failureMessage = failureMessage;
            return this;
        }

        public ExamConfiguration build() {
            return new ExamConfiguration(this);
        }
    }

    /**
     * @return The id of the exam
     */
    public UUID getExamId() {
        return examId;
    }

    /**
     * @return The timeout period of content load
     */
    public int getContentLoadTimeout() {
        return contentLoadTimeout;
    }

    /**
     * @return The timeout period for interface inactivity
     */
    public int getInterfaceTimeout() {
        return interfaceTimeout;
    }

    /**
     * @return The number of minutes after an exam is started where an exam is eligible for a resume
     */
    public int getExamRestartWindowMinutes() {
        return examRestartWindowMinutes;
    }

    /**
     * @return The number of pages to prefetch for the exam
     */
    public int getPrefetch() {
        return prefetch;
    }

    /**
     * @return The timeout period for proctor approval requests
     */
    public int getRequestInterfaceTimeout() {
        return requestInterfaceTimeout;
    }

    /**
     * @return The attempt number
     */
    public int getAttempt() {
        return attempt;
    }

    /**
     * @return The position of the exam to begin from
     */
    public int getStartPosition() {
        return startPosition;
    }

    /**
     * @return The status of the exam
     */
    public String getStatus() {
        return status;
    }

    /**
     * @return An optional failure message if the exam fails to start
     */
    public String getFailureMessage() {
        return failureMessage;
    }

    /**
     * @return The number of items in an exam
     */
    public int getTestLength() {
        return testLength;
    }

    /**
     * @return Flag for validating that all exam items are answered before submission
     */
    public boolean isValidateCompleteness() {
        return validateCompleteness;
    }
}
