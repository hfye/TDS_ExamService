package tds.exam.services.impl;

import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import tds.assessment.Algorithm;
import tds.assessment.Item;
import tds.assessment.ItemConstraint;
import tds.assessment.ItemProperty;
import tds.assessment.Segment;
import tds.assessment.Strand;
import tds.exam.ExamAccommodation;
import tds.exam.models.SegmentPoolInfo;
import tds.exam.services.ExamAccommodationService;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class SegmentPoolServiceImplTest {
    private SegmentPoolServiceImpl segmentPoolService;
    private ExamAccommodationService mockExamAccommodationService;


    @Before
    public void setUp() {
        mockExamAccommodationService = mock(ExamAccommodationService.class);
        segmentPoolService = new SegmentPoolServiceImpl(mockExamAccommodationService);
    }

    @Test
    public void shouldReturnSegmentPoolInfoForThreeItems(){
        final UUID examId = UUID.randomUUID();
        final String assessmentId = "my-assessment-id";
        final String segmentKey = "my-segment-key";
        Segment segment = new Segment(segmentKey);
        segment.setSelectionAlgorithm(Algorithm.ADAPTIVE_2);
        segment.setMinItems(5);
        segment.setMaxItems(13);

        Set<Strand> strands = new HashSet<>();
        Strand includedStrand1 = new Strand.Builder()
                .withName("included-strand1")
                .withMinItems(3)
                .withMaxItems(8)
                .withSegmentKey(segmentKey)
                .withAdaptiveCut(-32.123F)
                .build();
        Strand includedStrand2 = new Strand.Builder()
                .withName("included-strand2")
                .withMinItems(1)
                .withMaxItems(5)
                .withSegmentKey(segmentKey)
                .withAdaptiveCut(-37.523F)
                .build();
        Strand excludedStrand = new Strand.Builder()
                .withName("excluded-strand")
                .withMinItems(1)
                .withMaxItems(5)
                .withSegmentKey(segmentKey)
                .withAdaptiveCut(null)
                .build();

        strands.add(includedStrand1);
        strands.add(includedStrand2);
        strands.add(excludedStrand);

        segment.setStrands(strands);

        final String itemId1 = "item-1";
        final String itemId2 = "item-2";
        final String itemId3 = "item-3";
        final String esnItemId = "esn-item-3";
        final String ftItemId = "ft-item";
        final String excludedStrandItemId = "excluded-strand-item";

        List<ItemProperty> enuProps1 = new ArrayList<>();
        enuProps1.add(new ItemProperty("Language", "ENU", "English", itemId1));
        enuProps1.add(new ItemProperty("Language", "ENU-Braille", "Braille English", itemId1));
        enuProps1.add(new ItemProperty("--ITEMTYPE--", "ER", "Extended Response", itemId1));

        List<ItemProperty> enuProps2 = new ArrayList<>();
        enuProps2.add(new ItemProperty("Language", "ENU", "English", itemId2));
        enuProps2.add(new ItemProperty("Language", "ENU-Braille", "Braille English", itemId2));
        enuProps2.add(new ItemProperty("--ITEMTYPE--", "MI", "Matching Item", itemId2));

        List<ItemProperty> enuProps3 = new ArrayList<>();
        enuProps2.add(new ItemProperty("Language", "ENU", "English", itemId3));
        enuProps2.add(new ItemProperty("Language", "ENU-Braille", "Braille English", itemId3));
        enuProps2.add(new ItemProperty("--ITEMTYPE--", "MI", "Matching Item", itemId3));

        List<ItemProperty> ftProps = new ArrayList<>();
        enuProps2.add(new ItemProperty("Language", "ENU", "English", ftItemId));
        enuProps2.add(new ItemProperty("Language", "ENU-Braille", "Braille English", ftItemId));
        enuProps2.add(new ItemProperty("--ITEMTYPE--", "MI", "Matching Item", ftItemId));

        List<ItemProperty> excludedStrandItemProps = new ArrayList<>();
        enuProps2.add(new ItemProperty("Language", "ENU", "English", excludedStrandItemId));
        enuProps2.add(new ItemProperty("Language", "ENU-Braille", "Braille English", excludedStrandItemId));
        enuProps2.add(new ItemProperty("--ITEMTYPE--", "MI", "Matching Item", excludedStrandItemId));

        List<ItemProperty> esnProps = new ArrayList<>();
        // No ENU accommodation, should not be returned for itempool.
        esnProps.add(new ItemProperty("Language", "ESN", "Spanish", esnItemId));
        esnProps.add(new ItemProperty("--ITEMTYPE--", "MI", "Matching Item", esnItemId));


        List<Item> items = new ArrayList<>();
        Item item1 = new Item(itemId1);
        item1.setStrand(includedStrand1.getName());
        item1.setItemProperties(enuProps1);
        item1.setFieldTest(false);

        Item item2 = new Item(itemId2);
        item2.setStrand(includedStrand1.getName());
        item2.setItemProperties(enuProps2);
        item2.setFieldTest(false);

        Item item3 = new Item(itemId3);
        item3.setStrand(includedStrand1.getName());
        item3.setItemProperties(enuProps3);
        item3.setFieldTest(false);

        // Should be excluded by getItemPool for no ENU accommodation, not included in returned list of Ids
        Item esnItem = new Item(esnItemId);
        esnItem.setStrand(includedStrand2.getName());
        esnItem.setItemProperties(esnProps);
        esnItem.setFieldTest(false);

        // Should be included in the itemPoolIds list, but wont be factored into other calculations because its an FT item
        Item ftItem = new Item(ftItemId);
        ftItem.setStrand(includedStrand2.getName());
        ftItem.setItemProperties(ftProps);
        ftItem.setFieldTest(true);

        // This item will be included in the itemPoolIds list, but wont be factored into other calculations
        Item excludedStrandItem = new Item(excludedStrandItemId);
        excludedStrandItem.setItemProperties(excludedStrandItemProps);
        excludedStrandItem.setStrand(excludedStrand.getName());       // This should be excluded from strand calculations
        excludedStrandItem.setFieldTest(false);

        items.add(item1);
        items.add(item2);
        items.add(item3);
        items.add(ftItem);
        items.add(esnItem);
        items.add(excludedStrandItem);
        segment.setItems(items);

        List<ItemConstraint> itemConstraints = new ArrayList<>();
        itemConstraints.add(new ItemConstraint.Builder()
                .withAssessmentId(assessmentId)
                .withToolType("Language")
                .withToolValue("ENU")
                .withPropertyName("Language")
                .withPropertyValue("ENU")
                .withInclusive(true)
                .build());
        itemConstraints.add(new ItemConstraint.Builder()
                .withAssessmentId(assessmentId)
                .withToolType("--ITEMTYPE--")
                .withToolValue("ER")
                .withPropertyName("--ITEMTYPE")
                .withPropertyValue("ENU")
                .withInclusive(true)
                .build());
        itemConstraints.add(new ItemConstraint.Builder()
                .withAssessmentId(assessmentId)
                .withToolType("Language")
                .withToolValue("ESN")
                .withPropertyName("Language")
                .withPropertyValue("ESN")
                .withInclusive(false)
                .build());

        List<ExamAccommodation> examAccommodations = new ArrayList<>();
        examAccommodations.add(new ExamAccommodation.Builder()
                .withExamId(examId)
                .withType("Language")
                .withCode("ENU")
                .withDescription("English")
                .withSegmentKey(segmentKey)
                .build());
        examAccommodations.add(new ExamAccommodation.Builder()
                .withExamId(examId)
                .withType("type1")
                .withCode("TDS_T1")
                .withDescription("type 1 desc")
                .withSegmentKey(segmentKey)
                .build());

        when(mockExamAccommodationService.findAllAccommodations(examId)).thenReturn(examAccommodations);
        SegmentPoolInfo segmentPoolInfo = segmentPoolService.computeSegmentPool(examId, segment, itemConstraints);
        assertThat(segmentPoolInfo).isNotNull();
        assertThat(segmentPoolInfo.getItemPoolCount()).isEqualTo(4);
        assertThat(segmentPoolInfo.getLength()).isEqualTo(4);
        assertThat(segmentPoolInfo.getItemPoolIds()).contains(itemId1, itemId2, excludedStrandItemId, ftItemId);

    }

    @Test
    public void shouldFindTwoItems() {
        final UUID examId = UUID.randomUUID();
        final String segmentKey = "my-segment-key";
        final String assessmentId = "my-assessment-id";

        final String itemId1 = "item-1";
        final String itemId2 = "item-2";
        final String itemId3 = "item-3";

        List<ItemProperty> itemProperties1 = new ArrayList<>();
        itemProperties1.add(new ItemProperty("Language", "ENU", "English", itemId1));
        itemProperties1.add(new ItemProperty("Language", "ENU-Braille", "Braille English", itemId1));
        itemProperties1.add(new ItemProperty("--ITEMTYPE--", "ER", "Extended Response", itemId1));
        List<ItemProperty> itemProperties2 = new ArrayList<>();
        itemProperties2.add(new ItemProperty("Language", "ENU", "English", itemId2));
        itemProperties2.add(new ItemProperty("Language", "ENU-Braille", "Braille English", itemId2));
        itemProperties2.add(new ItemProperty("--ITEMTYPE--", "MI", "Matching Item", itemId2));
        List<ItemProperty> itemProperties3 = new ArrayList<>();
        // No ENU accommodation, should not be returned for itempool.
        itemProperties3.add(new ItemProperty("Language", "ESN", "Spanish", itemId3));
        itemProperties3.add(new ItemProperty("--ITEMTYPE--", "MI", "Matching Item", itemId3));

        List<Item> items = new ArrayList<>();
        Item item1 = new Item(itemId1);
        item1.setItemProperties(itemProperties1);
        Item item2 = new Item(itemId2);
        item2.setItemProperties(itemProperties2);
        Item item3 = new Item(itemId3);
        item3.setItemProperties(itemProperties3);
        items.add(item1);
        items.add(item2);
        items.add(item3);

        List<ItemConstraint> itemConstraints = new ArrayList<>();
        itemConstraints.add(new ItemConstraint.Builder()
                .withAssessmentId(assessmentId)
                .withToolType("Language")
                .withToolValue("ENU")
                .withPropertyName("Language")
                .withPropertyValue("ENU")
                .withInclusive(true)
                .build());
        itemConstraints.add(new ItemConstraint.Builder()
                .withAssessmentId(assessmentId)
                .withToolType("--ITEMTYPE--")
                .withToolValue("ER")
                .withPropertyName("--ITEMTYPE")
                .withPropertyValue("ENU")
                .withInclusive(true)
                .build());
        itemConstraints.add(new ItemConstraint.Builder()
                .withAssessmentId(assessmentId)
                .withToolType("Language")
                .withToolValue("ESN")
                .withPropertyName("Language")
                .withPropertyValue("ESN")
                .withInclusive(false)
                .build());

        List<ExamAccommodation> examAccommodations = new ArrayList<>();
        examAccommodations.add(new ExamAccommodation.Builder()
                .withExamId(examId)
                .withType("Language")
                .withCode("ENU")
                .withDescription("English")
                .withSegmentKey(segmentKey)
                .build());
        examAccommodations.add(new ExamAccommodation.Builder()
            .withExamId(examId)
            .withType("type1")
            .withCode("TDS_T1")
            .withDescription("type 1 desc")
            .withSegmentKey(segmentKey)
            .build());

        when(mockExamAccommodationService.findAllAccommodations(examId)).thenReturn(examAccommodations);

        Set<Item> retItemIds = segmentPoolService.getItemPool(examId, itemConstraints, items);
        verify(mockExamAccommodationService).findAllAccommodations(examId);
        assertThat(retItemIds).hasSize(2);
        assertThat(retItemIds).contains(item1, item2);
    }
}
