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
import tds.exam.services.ItemPoolService;
import tds.exam.services.SegmentPoolService;

@Service
public class SegmentPoolServiceImpl implements SegmentPoolService {
    private static final Logger LOG = LoggerFactory.getLogger(SegmentPoolServiceImpl.class);
    private ItemPoolService itemPoolService;

    @Autowired
    public SegmentPoolServiceImpl(ItemPoolService itemPoolService) {
        this.itemPoolService = itemPoolService;
    }

    @Override
    public SegmentPoolInfo computeSegmentPool(final UUID examId, final Segment segment,
                                              final List<ItemConstraint> itemConstraints) {
        // Get the list of eligible items based on constraints and exam accommodations
        Set<Item> itemPool = itemPoolService.getItemPool(examId, itemConstraints, segment.getItems());
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

    /**
     * Class to represent grouping of segment blueprint metadata
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
