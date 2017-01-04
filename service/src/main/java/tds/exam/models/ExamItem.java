package tds.exam.models;

/**
 * Represent the item on a page of an exam
 */
public class ExamItem {
    private long id;
    private String itemKey;
    private int position;
    private String type;
    private boolean fieldTest;
    private String segmentId;
    private boolean required;

    private ExamItem() {}

    public ExamItem(Builder builder) {
        this.id = builder.id;
        this.itemKey = builder.itemKey;
        this.position = builder.position;
        this.type = builder.type;
        this.fieldTest = builder.fieldTest;
        this.segmentId = builder.segmentId;
        this.required = builder.required;
    }

    public static final class Builder {
        private long id;
        private String itemKey;
        private int position;
        private String type;
        private boolean fieldTest;
        private String segmentId;
        private boolean required;

        public Builder withItemKey(String itemKey) {
            this.itemKey = itemKey;
            return this;
        }

        public Builder withPosition(int position) {
            this.position = position;
            return this;
        }

        public Builder withType(String type) {
            this.type = type;
            return this;
        }

        public Builder withFieldTest(boolean fieldTest) {
            this.fieldTest = fieldTest;
            return this;
        }

        public Builder withSegmentId(String segmentId) {
            this.segmentId = segmentId;
            return this;
        }

        public Builder withRequired(boolean required) {
            this.required = required;
            return this;
        }

        public ExamItem build() {
            return new ExamItem(this);
        }
    }

    /**
     * @return The id of the exam item
     */
    public long getId() {
        return id;
    }

    /**
     * @return The key of the item
     */
    public String getItemKey() {
        return itemKey;
    }

    /**
     * @return The position of the item in the exam
     */
    public int getPosition() {
        return position;
    }

    /**
     * @return The item type (e.g. MI for "Matching Item", MC for "Multiple Choice"
     */
    public String getType() {
        return type;
    }

    /**
     * @return Flag indicating whether this is a field test exam item
     */
    public boolean isFieldTest() {
        return fieldTest;
    }

    /**
     * @return The id of the {@link tds.assessment.Segment} the item belongs to
     */
    public String getSegmentId() {
        return segmentId;
    }

    /**
     * @return Flag indicating whether this is a required item.
     */
    public boolean isRequired() {
        return required;
    }

}
