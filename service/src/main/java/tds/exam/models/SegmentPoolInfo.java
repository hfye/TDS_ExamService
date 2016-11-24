package tds.exam.models;

import java.util.Set;

/**
 * A class that contains information about a computed segment pool.
 */
public class SegmentPoolInfo {
    private int length;
    private int poolCount;
    private Set<String> itemPoolIds;

    public SegmentPoolInfo(Builder builder) {
        this.length = builder.length;
        this.poolCount = builder.poolCount;
        this.itemPoolIds = builder.itemPoolIds;
    }

    public long getLength() {
        return length;
    }

    public int getItemPoolCount() {
        return poolCount;
    }

    public Set<String> getItemPoolIds() {
        return itemPoolIds;
    }

    public static final class Builder {
        private int length;
        private int poolCount;
        private Set<String> itemPoolIds;

        public Builder() {}

        public Builder withLength(int length) {
            this.length = length;
            return this;
        }

        public Builder withPoolCount(int poolCount) {
            this.poolCount = poolCount;
            return this;
        }

        public Builder withItemPoolIds(Set<String> itemPoolIds) {
            this.itemPoolIds = itemPoolIds;
            return this;
        }

        public SegmentPoolInfo build() {
            return new SegmentPoolInfo((this));
        }
    }
}
