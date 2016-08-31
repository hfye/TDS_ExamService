package tds.exam;

import java.util.UUID;

/**
 * Class representing an exam
 */
public class Exam {
    private final UUID id;

    public Exam(final UUID id) {
        this.id = id;
    }

    public UUID getId() {
        return id;
    }
}
