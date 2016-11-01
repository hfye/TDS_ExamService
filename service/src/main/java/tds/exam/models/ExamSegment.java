package tds.exam.models;

import tds.exam.Exam;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

/**
 * Represents the segment of an exam.
 *
 */
public class ExamSegment {
    private UUID examId;
    private String assessmentSegmentKey;
    private String assessmentSegmentId;
    private int segmentPosition;
    private String formKey;
    private String formId;
    private String algorithm;
    private int examItemCount;
    private int fieldTestItemCount;
    private List<String> fieldTestItems;
    private boolean isPermeable;
    private String restorePermeableOn;
    private String formCohort;
    private boolean isSatisfied;
    private Instant dateExited;
    private List<String> itemPool;
    private int poolCount;
    private Instant createdAt;

    public ExamSegment(Builder builder) {
        this.examId = builder.examId;
        this.assessmentSegmentKey = builder.assessmentSegmentKey;
        this.assessmentSegmentId = builder.assessmentSegmentId;
        this.segmentPosition = builder.segmentPosition;
        this.formKey = builder.formKey;
        this.formId = builder.formId;
        this.algorithm = builder.algorithm;
        this.examItemCount = builder.examItemCount;
        this.fieldTestItemCount = builder.fieldTestItemCount;
        this.fieldTestItems = builder.fieldTestItems;
        this.isPermeable = builder.isPermeable;
        this.restorePermeableOn = builder.restorePermeableOn;
        this.formCohort = builder.formCohort;
        this.isSatisfied = builder.isSatisfied;
        this.dateExited = builder.dateExited;
        this.itemPool = builder.itemPool;
        this.poolCount = builder.poolCount;
        this.createdAt = builder.createdAt;
    }

    public static class Builder {
        private UUID examId;
        private String assessmentSegmentKey;
        private String assessmentSegmentId;
        private int segmentPosition;
        private String formKey;
        private String formId;
        private String algorithm;
        private int examItemCount;
        private int fieldTestItemCount;
        private List<String> fieldTestItems;
        private boolean isPermeable;
        private String restorePermeableOn;
        private String formCohort;
        private boolean isSatisfied;
        private Instant dateExited;
        private List<String> itemPool;
        private int poolCount;
        private Instant createdAt;

        public Builder withExamId(UUID newExamId) {
            this.examId = newExamId;
            return this;
        }

        public Builder withAssessmentSegmentKey(String newAssessmentSegmentKey) {
            this.assessmentSegmentKey = newAssessmentSegmentKey;
            return this;
        }

        public Builder withAssessmentSegmentId(String newAssessmentSegmentId) {
            this.assessmentSegmentId = newAssessmentSegmentId;
            return this;
        }

        public Builder withSegmentPosition(int newSegmentPosition) {
            this.segmentPosition = newSegmentPosition;
            return this;
        }

        public Builder withFormKey(String newFormKey) {
            this.formKey = newFormKey;
            return this;
        }

        public Builder withFormId(String newFormId) {
            this.formId = newFormId;
            return this;
        }

        public Builder withAlgorithm(String newAlgorithm) {
            this.algorithm = newAlgorithm;
            return this;
        }

        public Builder withExamItemCount(int newExamItemCount) {
            this.examItemCount = newExamItemCount;
            return this;
        }

        public Builder withFieldTestItemCount(int newFieldTestItemCount) {
            this.fieldTestItemCount = newFieldTestItemCount;
            return this;
        }

        public Builder withFieldTestItems(List<String> newFieldTestItems) {
            this.fieldTestItems = newFieldTestItems;
            return this;
        }

        public Builder withIsPermeable(boolean newIsPermeable) {
            this.isPermeable = newIsPermeable;
            return this;
        }

        public Builder withRestorePermeableOn(String newRestorePermeableOn) {
            this.restorePermeableOn = newRestorePermeableOn;
            return this;
        }

        public Builder withFormCohort(String newFormCohort) {
            this.formCohort = newFormCohort;
            return this;
        }

        public Builder withIsSatisfied(boolean newIsSatisfied) {
            this.isSatisfied = newIsSatisfied;
            return this;
        }

        public Builder withDateExited(Instant newDateExited) {
            this.dateExited = newDateExited;
            return this;
        }

        public Builder withItemPool(List<String> newItemPool) {
            this.itemPool = newItemPool;
            return this;
        }

        public Builder withPoolCount(int newPoolCount) {
            this.poolCount = newPoolCount;
            return this;
        }

        public Builder withCreatedAt(Instant newCreatedAt) {
            this.createdAt = newCreatedAt;
            return this;
        }

        public ExamSegment build() {
            return new ExamSegment(this);
        }
    }


    public int getSegmentPosition() {
        return segmentPosition;
    }

    public UUID getExamId() {
        return examId;
    }

    public String getAssessmentSegmentKey() {
        return assessmentSegmentKey;
    }

    public String getAssessmentSegmentId() {
        return assessmentSegmentId;
    }

    public String getFormKey() {
        return formKey;
    }

    public String getFormId() {
        return formId;
    }

    public String getAlgorithm() {
        return algorithm;
    }

    public int getExamItemCount() {
        return examItemCount;
    }

    public int getFieldTestItemCount() {
        return fieldTestItemCount;
    }

    public List<String> getFieldTestItems() {
        return fieldTestItems;
    }

    public boolean isPermeable() {
        return isPermeable;
    }

    public String getRestorePermeableOn() {
        return restorePermeableOn;
    }

    public String getFormCohort() {
        return formCohort;
    }

    public boolean isSatisfied() {
        return isSatisfied;
    }

    public Instant getDateExited() {
        return dateExited;
    }

    public List<String> getItemPool() {
        return itemPool;
    }

    public int getPoolCount() {
        return poolCount;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}
