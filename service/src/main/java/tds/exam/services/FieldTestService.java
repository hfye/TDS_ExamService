package tds.exam.services;

import tds.assessment.Assessment;
import tds.exam.Exam;

/**
 * Created by emunoz on 11/6/16.
 */
public interface FieldTestService {

    boolean isFieldTestEligible(Exam exam, Assessment assessment, String segmentKey, String language,
                                String environment);
}
