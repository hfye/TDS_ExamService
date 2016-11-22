package tds.exam.services.impl;

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
    private AssessmentService assessmentService;
    private ExamAccommodationService examAccommodationService;

    @Autowired
    public SegmentPoolServiceImpl(AssessmentService assessmentService,
                                  ExamAccommodationService examAccommodationService) {
        this.assessmentService = assessmentService;
        this.examAccommodationService = examAccommodationService;
    }

    @Override
    public SegmentPoolInfo computeSegmentPool(UUID examId, Segment segment, List<ItemConstraint> itemConstraints) {

        /* getItemPool selects the items that are eligible for the segment pool we are constructing.
           In legacy code, we can skip a lot of the temp-table initialization logic because of this */
        Set<Item> itemPool = getItemPool(examId, itemConstraints, segment.getItems());
        Set<Strand> strands = segment.getStrands();
        List<SegmentBluePrint> segmentBluePrints = new ArrayList<>();

        for (Item item : itemPool) {
            Optional<Strand> maybeItemStrand = strands.stream()
                    .filter(strand -> strand.getName().equals(item.getStrand()))
                    .findFirst();

            if (maybeItemStrand.isPresent()) {
                /* LN 2895 - they join a temp table with itemstrands. Since we have the itemstrands and item info
                    already in the assessment object, we can get the poolcount here */
                Strand itemStrand = maybeItemStrand.get();
                int poolCount = (int) itemPool.stream()
                        .filter(innerItem -> !innerItem.isFieldTest() &&
                                innerItem.getStrand().equals(itemStrand.getName()))
                        .count();

                segmentBluePrints.add(new SegmentBluePrint(
                        item.getStrand(),
                        itemStrand.getMinItems(),
                        itemStrand.getMaxItems(),
                        poolCount));
            } else {
                //TODO: Throw exception? Log error?
            }
        }

        /* LN 2887,2914:
            sessionKey is null only when we are not in simulation mode. See line 4622.
            TODO: Skip the conditional branch of code [2914-2938] until simulation mode is implemented
         */

        /* [2904] Realistically, this should always be 'adaptive2' here, but we'll follow their conditional logic anyway */
        //TODO: Remove this magic string - create algorithm enum??
        int testLength = "adaptive2".equals(segment.getSelectionAlgorithm()) ? segment.getMaxItems() : segment.getMinItems();
        /* [2939] select convert(sum(minitems - poolcnt), SIGNED) as shortfall from ${bluePrintTable} where poolcnt < minitems */
        int fallback = (int) segmentBluePrints.stream()
                .filter(pool -> pool.getPoolCount() < pool.getMinItems())
                .mapToLong(pool -> pool.getMinItems() - pool.getPoolCount())
                .sum();
        /* Get the sum of all the strands: select convert(sum(poolcnt), SIGNED) as strandcnt from ${bluePrintTable}; */
        int strandCount = (int) segmentBluePrints.stream()
                .mapToLong(SegmentBluePrint::getPoolCount)
                .sum();
        int lengthDelta = testLength - fallback;
        int newLength;
        if (LegacyComparer.lessThan(lengthDelta, strandCount)) {
            newLength =  lengthDelta > 0 ? lengthDelta : testLength;
        } else {
            newLength = strandCount;
        }

        Set<String> itemPoolIds = itemPool.stream().map(Item::getId).collect(Collectors.toSet());

        return new SegmentPoolInfo.Builder()
                .withItemPoolIds(itemPoolIds)
                .withPoolCount(strandCount)
                .withLength(newLength)
                .build();
    }

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

//        Set<ExamAccommodation> excludedAccommodations = allAccommodations.stream()
//                .flatMap(accommodation -> itemConstraints.stream()
//                        .filter(itemConstraint -> !itemConstraint.isInclusive() &&
//                                itemConstraint.getPropertyName().equals(accommodation.getType()) &&
//                                itemConstraint.getPropertyValue().equals(accommodation.getCode()))
//                        .map(itemConstraint -> accommodation))
//                .collect(Collectors.toSet());

        // For the included accommodations above, find the list of compatible item ids
        Set<String> itemPoolIds = itemProperties.stream()
                .flatMap(itemProperty -> includedAccommodations.stream()
                        .filter(accommodation ->
                                itemProperty.getName().equals(accommodation.getType()) &&
                                itemProperty.getValue().equals(accommodation.getCode()))
                        .map(accommodation -> itemProperty.getItemId()))
                .collect(Collectors.toSet());

        //TODO: filter out exclusions and perhaps remove intermediary steps

        Set<Item> itemPool = items.stream().
                filter(item -> itemPoolIds.contains(item.getId()))
                .collect(Collectors.toSet());
        return itemPool;
    }

    private class SegmentBluePrint {
        private String strand;
        private int minItems;
        private int maxItems;
        private long poolCount;

        public SegmentBluePrint(String strand, int minItems, int maxItems, long poolCount) {
            this.strand = strand;
            this.minItems = minItems;
            this.maxItems = maxItems;
            this.poolCount = poolCount;
        }

        public long getPoolCount() {
            return this.poolCount;
        }

        public int getMinItems() {
            return this.minItems;
        }
    }
}
