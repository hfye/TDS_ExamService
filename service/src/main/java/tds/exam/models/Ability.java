package tds.exam.models;

import java.time.Instant;
import java.util.UUID;

/**
 * Created by emunoz on 10/3/16.
 */
public class Ability {
    private UUID examId;

    private String test;

    private Integer timesTaken;

    private Instant dateScored;

    private Float score;

    public UUID getExamId() {
        return examId;
    }

    public void setExamId(UUID examId) {
        this.examId = examId;
    }

    public String getTest() {
        return test;
    }

    public void setTest(String test) {
        this.test = test;
    }

    public Integer getTimesTaken() {
        return timesTaken;
    }

    public void setTimesTaken(Integer timesTaken) {
        this.timesTaken = timesTaken;
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
