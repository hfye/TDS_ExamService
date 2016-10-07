package tds.exam.models;

import java.time.Instant;
import java.util.UUID;

/**
 * Created by emunoz on 10/3/16.
 */
public class Ability {
    private UUID examId;

    private String assessment;

    private Integer attempts;

    private Instant dateScored;

    private Float score;

    public UUID getExamId() {
        return examId;
    }

    public void setExamId(UUID examId) {
        this.examId = examId;
    }

    public String getAssessment() {
        return assessment;
    }

    public void setAssessment(String assessment) {
        this.assessment = assessment;
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
