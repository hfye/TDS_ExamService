package tds.exam.services.impl;

import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import tds.assessment.Item;
import tds.assessment.ItemConstraint;
import tds.assessment.ItemProperty;
import tds.exam.Exam;
import tds.exam.ExamAccommodation;
import tds.exam.services.AssessmentService;
import tds.exam.services.ExamAccommodationService;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class SegmentPoolServiceImplTest {
    private SegmentPoolServiceImpl segmentPoolService;
    private AssessmentService mockAssessmentService;
    private ExamAccommodationService mockExamAccommodationService;


    @Before
    public void setUp() {
        mockAssessmentService = mock(AssessmentService.class);
        mockExamAccommodationService = mock(ExamAccommodationService.class);
        segmentPoolService = new SegmentPoolServiceImpl(mockAssessmentService, mockExamAccommodationService);
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

        Set<String> retItemIds = segmentPoolService.getItemPool(examId, itemConstraints, items);
        assertThat(retItemIds).hasSize(2);
        assertThat(retItemIds).contains(itemId1, itemId2);
    }
}
