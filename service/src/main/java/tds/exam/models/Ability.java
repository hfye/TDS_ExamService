package tds.exam.models;

import java.time.Instant;
import java.util.UUID;

/**
 * Model representing an exam's ability.
 */
public class Ability {
    /**
     * The id of the exam
     */
    private UUID examId;

    /**
     * The assessment id
     */
    private String assessmentId;

    /**
     * The attempt number of the exam
     */
    private Integer attempts;

    /**
     * The date when the exam was scored
     */
    private Instant dateScored;

    /**
     * The ability score value
     */
    private Float score;

    public UUID getExamId() {
        return examId;
    }

    public void setExamId(UUID examId) {
        this.examId = examId;
    }

    public String getAssessmentId() {
        return assessmentId;
    }

    public void setAssessmentId(String assessmentId) {
        this.assessmentId = assessmentId;
    }

    public Integer getAttempts() {
        return attempts;
    }

    public void setAttempts(Integer attempts) {
        this.attempts = attempts;
    }

    public Instant getDateScored() {
        return dateScored;
    }

    public void setDateScored(Instant dateScored) {
        this.dateScored = dateScored;
    }

    public Float getScore() {
        return score;
    }

    public void setScore(Float score) {
        this.score = score;
    }
}
