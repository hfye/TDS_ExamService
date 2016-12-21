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
        private int testLength;
        private boolean validateCompleteness;

        public Builder() {
        }

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

        public ExamConfiguration build() {
            return new ExamConfiguration(this);
        }
    }

    public UUID getExamId() {
        return examId;
    }

    public int getContentLoadTimeout() {
        return contentLoadTimeout;
    }

    public int getInterfaceTimeout() {
        return interfaceTimeout;
    }

    public int getExamRestartWindowMinutes() {
        return examRestartWindowMinutes;
    }

    public int getPrefetch() {
        return prefetch;
    }

    public int getRequestInterfaceTimeout() {
        return requestInterfaceTimeout;
    }

    public int getAttempt() {
        return attempt;
    }

    public int getStartPosition() {
        return startPosition;
    }

    public String getStatus() {
        return status;
    }

    public int getTestLength() {
        return testLength;
    }

    public boolean isValidateCompleteness() {
        return validateCompleteness;
    }
}
