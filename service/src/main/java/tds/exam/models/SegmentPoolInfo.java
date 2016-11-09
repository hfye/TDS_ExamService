package tds.exam.models;

import java.util.List;

/**
 * A class that contains information about a segment pool.
 */
public class SegmentPoolInfo {
    private Integer newLength;
    private Integer itemPoolCount;
    private List<String> items;

    public SegmentPoolInfo(Builder builder) {
        this.newLength = builder.newLength;
        this.itemPoolCount = builder.itemPoolCount;
        this.items = builder.items;
    }

    public Integer getNewLength() {
        return newLength;
    }

    public Integer getItemPoolCount() {
        return itemPoolCount;
    }

    public List<String> getItems() {
        return items;
    }

    public static final class Builder {
        private Integer newLength;
        private Integer itemPoolCount;
        private List<String> items;

        private Builder() {
        }

        public static Builder aSegmentPoolInfo() {
            return new Builder();
        }

        public Builder withNewLength(int newLength) {
            this.newLength = newLength;
            return this;
        }

        public Builder withItemPoolCount(int itemPoolCount) {
            this.itemPoolCount = itemPoolCount;
            return this;
        }

        public Builder withItems(List<String> items) {
            this.items = items;
            return this;
        }

        public SegmentPoolInfo build() {
            return new SegmentPoolInfo((this));
        }
    }
}
