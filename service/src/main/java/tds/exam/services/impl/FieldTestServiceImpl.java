package tds.exam.services.impl;

import org.joda.time.Instant;
import org.springframework.stereotype.Service;

import tds.assessment.Assessment;
import tds.assessment.Segment;
import tds.config.Accommodation;
import tds.exam.Exam;
import tds.exam.services.FieldTestService;
import tds.session.ExternalSessionConfiguration;

@Service
public class FieldTestServiceImpl implements FieldTestService {

    @Override
    public boolean isFieldTestEligible(Exam exam, Assessment assessment, String segmentKey, String languageCode) {
        boolean isEligible = false;
        Segment currentSegment = assessment.getSegment(segmentKey);

        // Get the count items that are compatible with the selected language
        /* StudentDLL [4430] */
        int fieldTestItemCount = (int) currentSegment.getItems().stream()
            .filter(item ->
                item.isFieldTest() &&
                item.getItemProperties().stream()
                    .filter(prop ->
                        prop.getName().equalsIgnoreCase(Accommodation.ACCOMMODATION_TYPE_LANGUAGE) &&
                        prop.getValue().equalsIgnoreCase(languageCode))
                    .findFirst().isPresent())
            .count();

        /* This is line 4437 and 4453 combined into one conditional */
        if (fieldTestItemCount > 0 && currentSegment.getFieldTestMinItems() > 0) {
            if (ExternalSessionConfiguration.SIMULATION_ENVIRONMENT.equalsIgnoreCase(exam.getEnvironment())) {
                isEligible = true;
            } else {
                /* Line [4473] : client_testproperties already included in Assessment */
                boolean assessmentEligible = isWithinFieldTestWindow(assessment.getFieldTestStartDate(), assessment.getFieldTestEndDate());
                /* parentKey == testKey when the assessment is non-segmented */
                if (!assessment.isSegmented() || !assessmentEligible) {
                    return assessmentEligible;
                }
                /* Line [4491]  : client_segmentproperties already included in Segments */
                isEligible = isWithinFieldTestWindow(currentSegment.getFieldTestStartDate(), currentSegment.getFieldTestEndDate());
            }
        }

        return isEligible;
    }

    /*
        This helper method is a null-tolerant Instant/date comparison for the test window
     */
    private boolean isWithinFieldTestWindow(Instant startTime, Instant endTime) {
        boolean inWindow;
        // null field test start/end times are considered "always open" windows
        if (startTime != null) {
            if (!startTime.isBeforeNow()) {
                return false;
            }
            inWindow = endTime == null ? true : endTime.isAfterNow();
        } else {
            inWindow = endTime == null ? true : endTime.isAfterNow();
        }

        return inWindow;
    }
}
