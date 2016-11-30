package tds.exam.services.impl;

import org.joda.time.Instant;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.Optional;

import tds.assessment.Assessment;
import tds.assessment.Segment;
import tds.common.data.legacy.LegacyComparer;
import tds.config.ClientSegmentProperty;
import tds.config.ClientTestProperty;
import tds.exam.Exam;
import tds.exam.services.ConfigService;
import tds.exam.services.FieldTestService;

@Service
public class FieldTestServiceImpl implements FieldTestService {
    private ConfigService configService;

    @Autowired
    public FieldTestServiceImpl(ConfigService configService) {
        this.configService = configService;
    }

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
                            prop.getName().equals("Language") &&
                            prop.getValue().equals(languageCode))
                        .findFirst().isPresent())
                .count();

        // This is line 4437 and 4453 combined into one conditional
        //TODO: Move to enum/const
        if (fieldTestItemCount > 0 && currentSegment.getFieldTestMinItems() > 0)
            if ("SIMULATION".equals(exam.getEnvironment())) {
                isEligible = true;
            } else {
                // Check assessment config properties for field test time window
                Optional<ClientTestProperty> maybeProperty = configService.findClientTestProperty(exam.getClientName(),
                    assessment.getAssessmentId());

                if (maybeProperty.isPresent()) {
                    ClientTestProperty property = maybeProperty.get();

                    boolean assessmentEligible = isWithinFieldTestWindow(property.getFieldTestStartDate(), property.getFieldTestEndDate());
                    /* parentKey == testKey when the assessment is non-segmented */
                    if (!assessment.isSegmented() || !assessmentEligible) {
                        return assessmentEligible;
                    }

                    // if it is segmented, check the segment properties for field test time window
                    Optional<ClientSegmentProperty> maybeSegmentProperty =
                        configService.findClientSegmentProperty(exam.getClientName(), currentSegment.getSegmentId());

                    if (maybeSegmentProperty.isPresent()) {
                        ClientSegmentProperty segmentProperty = maybeSegmentProperty.get();
                        isEligible = isWithinFieldTestWindow(segmentProperty.getFieldTestStartDate(), segmentProperty.getFieldTestEndDate());
                    } else {
                        throw new IllegalArgumentException(String.format("No client test property found for client %s with segment id %s",
                            exam.getClientName(), currentSegment.getSegmentId()));
                    }
                } else {
                    throw new IllegalArgumentException(String.format("No client test property found for client %s with assessment id %s",
                        exam.getClientName(), assessment.getAssessmentId()));
                }
            }

        return isEligible;
    }


    private boolean isWithinFieldTestWindow(Instant startTime, Instant endTime) {
        boolean inWindow;

        // null ft start/end times are considered "always open" windows
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
