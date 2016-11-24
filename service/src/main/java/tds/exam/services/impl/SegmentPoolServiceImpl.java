package tds.exam.services.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.function.Predicate;
import java.util.stream.Collector;
import java.util.stream.Collectors;

import tds.assessment.Algorithm;
import tds.assessment.Assessment;
import tds.assessment.Item;
import tds.assessment.ItemConstraint;
import tds.assessment.ItemProperty;
import tds.assessment.Segment;
import tds.assessment.Strand;
import tds.common.data.legacy.LegacyComparer;
import tds.exam.Exam;
import tds.exam.ExamAccommodation;
import tds.exam.models.SegmentPoolInfo;
import tds.exam.services.AssessmentService;
import tds.exam.services.ExamAccommodationService;
import tds.exam.services.SegmentPoolService;

/**
 * Created by emunoz on 11/7/16.
 */
@Service
public class SegmentPoolServiceImpl implements SegmentPoolService {
    private static final Logger LOG = LoggerFactory.getLogger(SegmentPoolServiceImpl.class);
    private ExamAccommodationService examAccommodationService;

    @Autowired
    public SegmentPoolServiceImpl(ExamAccommodationService examAccommodationService) {
        this.examAccommodationService = examAccommodationService;
    }

    @Override
    public SegmentPoolInfo computeSegmentPool(UUID examId, Segment segment, List<ItemConstraint> itemConstraints) {
        // Get the list of eligible items based on constraints and exam accommodations
        Set<Item> itemPool = getItemPool(examId, itemConstraints, segment.getItems());
        /* getItemPool selects the items that are eligible for the segment pool we are constructing.
           In legacy code, we can skip a lot of the temp-table initialization logic because of this */
        Set<Strand> strands = segment.getStrands();
        List<SegmentBluePrint> segmentBluePrints = new ArrayList<>();

        for (Item item : itemPool) {
            // Find items with matching strands and non-null adaptiveCut value
            Optional<Strand> maybeItemStrand = strands.stream()
                    .filter(strand ->
                            strand.getName().equals(item.getStrand()) &&
                            strand.getAdaptiveCut() != null)
                    .findFirst();

            if (maybeItemStrand.isPresent()) {
                Strand itemStrand = maybeItemStrand.get();
                /* LN 2895 - they join a temp table with itemstrands. Since we have the strands and item info
                    already in the assessment object, we can get the poolcount here */
                // Get the count of (non-field test) items that have the same strand value
                int poolCount = (int) itemPool.stream()
                        .filter(innerItem ->
                                !innerItem.isFieldTest() &&
                                innerItem.getStrand().equals(itemStrand.getName()))
                        .count();

                segmentBluePrints.add(new SegmentBluePrint(
                        item.getStrand(),
                        itemStrand.getMinItems(),
                        poolCount));
            } else {
                LOG.warn(String.format("No strand match for item with id \"{}\" and strand \"{}\". Unable to add to segment pool computation"),
                        item.getId(), item.getStrand());
            }
        }

        /* [2887,2914]: sessionKey is null only when we are not in simulation mode. See line 4622.
            TODO: Skip the conditional branch of code [2914-2938] until simulation mode is implemented  */

        /* [2904] Realistically, this should always be 'adaptive2' here, but we'll follow legacy conditional logic anyway */
        int testLength = Algorithm.ADAPTIVE_2.equals(segment.getSelectionAlgorithm()) ? segment.getMaxItems() : segment.getMinItems();
        /* [2939] select convert(sum(minitems - poolcnt), SIGNED) as shortfall from ${bluePrintTable} where poolcnt < minitems */
        int shortfall = segmentBluePrints.stream()
                .filter(bluePrint -> bluePrint.getPoolCount() < bluePrint.getMinItems())
                .mapToInt(bluePrint -> bluePrint.getMinItems() - bluePrint.getPoolCount())
                .sum();
        /* [1885] Get the sum of all the strands: select convert(sum(poolcnt), SIGNED) as strandcnt from ${bluePrintTable}; */
        int strandCount = segmentBluePrints.stream()
                .mapToInt(SegmentBluePrint::getPoolCount)
                .sum();
        int lengthDelta = testLength - shortfall;
        int newLength = lengthDelta < strandCount ?
                lengthDelta > 0 ?
                        lengthDelta :
                        testLength
                : strandCount;

        Set<String> itemPoolIds = itemPool.stream().map(Item::getId).collect(Collectors.toSet());

        return new SegmentPoolInfo.Builder()
                .withItemPoolIds(itemPoolIds)
                .withPoolCount(strandCount)
                .withLength(newLength)
                .build();
    }

    /*
        This method is meant to replace StudentDLL._AA_ItempoolString_FNOptimized() [1643]
        The purpose of this method is to find the list of items to include in the segment by taking the following steps:

        1. Retrieve the accommodations that the student has enabled
        2. Find the matching set of (inclusive) item constraints - typically this is "Language"
        3. Find the set of items that satisfy/match the inclusive item constraints
        4. Exclude the items that match the "excluded" accommodations (based on constraints)
     */
    public Set<Item> getItemPool(UUID examId, List<ItemConstraint> itemConstraints, List<Item> items) {
        List<ExamAccommodation> allAccommodations = examAccommodationService.findAllAccommodations(examId);
        List<ItemProperty> itemProperties = items.stream()
                .flatMap(item -> item.getItemProperties().stream())
                .collect(Collectors.toList());

        // First, get all the constraints for our exam accommodations marked as "inclusive"
        Set<ExamAccommodation> includedAccommodations = allAccommodations.stream()
            .flatMap(accommodation -> itemConstraints.stream()
                .filter(itemConstraint -> itemConstraint.isInclusive() &&
                        itemConstraint.getPropertyName().equals(accommodation.getType()) &&
                        itemConstraint.getPropertyValue().equals(accommodation.getCode()))
                .map(itemConstraint -> accommodation))
            .collect(Collectors.toSet());

        Set<ExamAccommodation> excludedAccommodations = allAccommodations.stream()
                .flatMap(accommodation -> itemConstraints.stream()
                        .filter(itemConstraint -> !itemConstraint.isInclusive() &&
                                itemConstraint.getPropertyName().equals(accommodation.getType()) &&
                                itemConstraint.getPropertyValue().equals(accommodation.getCode()))
                        .map(itemConstraint -> accommodation))
                .collect(Collectors.toSet());

        // For the included accommodations above, find the list of compatible item ids
        Set<String> itemPoolIds = itemProperties.stream()
                .flatMap(itemProperty -> includedAccommodations.stream()
                        .filter(accommodation ->
                                itemProperty.getName().equals(accommodation.getType()) &&
                                itemProperty.getValue().equals(accommodation.getCode()))
                        .map(accommodation -> itemProperty.getItemId()))
                .collect(Collectors.toSet());

        //TODO: filter out exclusions and perhaps remove intermediary steps

        Set<Item> itemPool = items.stream()
                .filter(item -> itemPoolIds.contains(item.getId()))
                .collect(Collectors.toSet());

        return itemPool;
    }

    /**
     * Private class to represent grouping of segment blueprint metadata
     */
    private class SegmentBluePrint {
        private String strand;
        private int minItems;
        private int poolCount;

        public SegmentBluePrint(String strand, int minItems, int poolCount) {
            this.strand = strand;
            this.minItems = minItems;
            this.poolCount = poolCount;
        }

        public String getStrand() {
            return this.strand;
        }

        public int getPoolCount() {
            return this.poolCount;
        }

        public int getMinItems() {
            return this.minItems;
        }
    }
}
