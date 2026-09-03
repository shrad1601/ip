package taterror.task;

/**
 * A single task tracked by TA Terror. Common state and formatting logic shared
 * by {@link Todo}, {@link Deadline}, and {@link Event}; each subclass supplies
 * its own type code and any extra fields via the abstract methods below.
 */
public abstract class Task {
    private final String description;
    private boolean isDone;

    protected Task(String description) {
        this.description = description;
    }

    /**
     * Marks this task as done.
     */
    public void markAsDone() {
        isDone = true;
    }

    /**
     * Marks this task as not done.
     */
    public void markAsNotDone() {
        isDone = false;
    }

    public boolean isDone() {
        return isDone;
    }

    public String getDescription() {
        return description;
    }

    /**
     * Returns the single-letter code identifying this task's type in the save
     * file: T, D, or E.
     */
    public abstract String getTypeCode();

    /**
     * Returns this task's type-specific fields, formatted for the save file
     * (already including a leading " | " separator per field), or "" if it
     * has none.
     */
    public abstract String toSaveDetail();

    /**
     * Returns this task's type-specific detail text for display (e.g.
     * "(by: ...)"), or "" if it has none.
     */
    public abstract String toDisplayDetail();

    /**
     * Renders this task the way it appears in a task list, e.g.
     * {@code [T][X] read book} or {@code [D][ ] submit report (by: Sep 01 2026)}.
     */
    @Override
    public String toString() {
        String status = isDone ? "[X]" : "[ ]";
        String base = "[" + getTypeCode() + "]" + status + " " + description;
        String detail = toDisplayDetail();
        return detail.isEmpty() ? base : base + " " + detail;
    }
}
