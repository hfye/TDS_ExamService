package tds.exam.services.impl;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import tds.assessment.Assessment;
import tds.assessment.Item;
import tds.assessment.Segment;
import tds.exam.Exam;
import tds.exam.models.FieldTestItemGroup;
import tds.exam.services.FieldTestItemGroupSelector;
import tds.exam.services.ItemPoolService;

@Component
public class EqualDistributionFieldTestItemGroupSelector implements FieldTestItemGroupSelector {
    private final ItemPoolService itemPoolService;
    // Keeps track of the field test item groups and their usages
    private Cache<String, List<FieldTestItemGroupCounter>> fieldTestItemGroupCounterCache;

    @Autowired
    public EqualDistributionFieldTestItemGroupSelector(ItemPoolService itemPoolService) {
        this.itemPoolService = itemPoolService;

        fieldTestItemGroupCounterCache = CacheBuilder.newBuilder()
            .expireAfterAccess(1, TimeUnit.DAYS)
            .build();
    }

    @Override
    public List<FieldTestItemGroup> selectItemGroupsLeastUsed(final Exam exam, final Set<String> assignedGroupIds,
                                                              final Assessment assessment, final String segmentKey, final int numItems) {
        int ftItemCount = 0;
        Segment currentSegment = assessment.getSegment(segmentKey);
        // Fetch every eligible item based on item constraints, item properties, and user accommodations
        Set<Item> fieldTestItems = itemPoolService.getItemPool(exam.getId(), assessment.getItemConstraints(), currentSegment.getItems(exam.getLanguageCode()), true);
        /* In StudentDLL.FT_Prioritize2012_SP() [3544] - The legacy code creates temporary tables and groups data by groupid, blockid, groupkey.
            We can just worry about grouping by groupkey since groupKey appears to be the same as "<group-id>_<block-id>".

            The deletion at line [3186] simply removes item groups already found to be assigned to a student
                - the filter below will take care of this.
         */
        Map<String, List<FieldTestItemGroup>> fieldTestItemGroupsMap = fieldTestItems.stream()
            .filter(fieldTestItem -> !assignedGroupIds.contains(fieldTestItem.getGroupId()))    // Filter all previously assigned item groups
            .map(fieldTestItem -> new FieldTestItemGroup.Builder()
                .withExamId(exam.getId())
                .withGroupId(fieldTestItem.getGroupId())
                .withGroupKey(fieldTestItem.getGroupKey())
                .withBlockId(fieldTestItem.getBlockId())
                .build())
            .collect(Collectors.groupingBy(FieldTestItemGroup::getGroupKey));

        // If the cache does not contain any group key counters for this segment, add all of the groups this
        // student is eligible for
        if (!fieldTestItemGroupCounterCache.asMap().containsKey(segmentKey)) {
            List<FieldTestItemGroupCounter> initGroupCounters = fieldTestItemGroupsMap.keySet().stream()
                .map(groupKey -> new FieldTestItemGroupCounter(groupKey))
                .collect(Collectors.toList());

            fieldTestItemGroupCounterCache.put(segmentKey, Collections.synchronizedList(initGroupCounters));
        }

        List<FieldTestItemGroup> itemGroupsWithItemCounts = new ArrayList<>();
        // Get the list (sorted by number of group key occurrences) and starting at the top, add as many as we need to the list
        List<FieldTestItemGroupCounter> fieldTestItemGroupCounters = fieldTestItemGroupCounterCache.getIfPresent(segmentKey);
        for (FieldTestItemGroupCounter groupCounter : fieldTestItemGroupCounters) {
            // Break out of this loop if we've selected the # of items we needed to select
            if (ftItemCount >= numItems) {
                break;
            }

            // Check that the groupKey is one of the ones this examinee is eligible for
            if (fieldTestItemGroupsMap.containsKey(groupCounter.getGroupKey())) {
                List<FieldTestItemGroup> items = fieldTestItemGroupsMap.get(groupCounter.getGroupKey());
                // Since we are only concerned with data shared between all the items in the group, we can just pick the first
                FieldTestItemGroup firstItemGroup = items.get(0);

                itemGroupsWithItemCounts.add(new FieldTestItemGroup.Builder()
                    .fromFieldTestItemGroup(firstItemGroup)
                    .withNumItems(items.size())
                    .build());

                // Update the occurrence counter and the field test item count
                groupCounter.incrementOccurrance();
                ftItemCount += items.size();
                // Remove group from the map so we can keep track of any field test item groups that might need to be added to the cache
                fieldTestItemGroupsMap.remove(groupCounter.getGroupKey());
            }
        }

        // Check if there are any groups that may need to be added to cache (initialized)
        if (!fieldTestItemGroupsMap.isEmpty()) {
            cacheInitializedCounters(segmentKey, fieldTestItemGroupsMap);
        }

        Collections.sort(fieldTestItemGroupCounters);

        return itemGroupsWithItemCounts;
    }

    private void cacheInitializedCounters(String segmentKey, Map<String, List<FieldTestItemGroup>> fieldTestItemGroupsMap) {
        // We may need to initialize these group key counters and cache if they are not already cached
        List<FieldTestItemGroupCounter> groupCounters = fieldTestItemGroupCounterCache.getIfPresent(segmentKey);
        Set<String> cachedGroupKeys = groupCounters.stream()
            .map(counter -> counter.getGroupKey())
            .collect(Collectors.toSet());

        for (String groupKey : fieldTestItemGroupsMap.keySet()) {
            if (!cachedGroupKeys.contains(groupKey)) {
                groupCounters.add(new FieldTestItemGroupCounter(groupKey));
            }
        }
    }

    private class FieldTestItemGroupCounter implements Comparable<FieldTestItemGroupCounter> {
        private String groupKey;
        AtomicInteger occurrences;

        public FieldTestItemGroupCounter(String groupKey) {
            this.groupKey = groupKey;
            occurrences = new AtomicInteger(0);
        }

        public void incrementOccurrance() {
            occurrences.incrementAndGet();
        }

        public String getGroupKey() {
            return this.groupKey;
        }

        @Override
        public int compareTo(FieldTestItemGroupCounter other) {
            return occurrences.get() - other.occurrences.get();
        }
    }
}
