/**
 * A task with just a description and no date/time attached.
 */
public class Todo extends Task {
    public Todo(String description) {
        super(description);
    }

    @Override
    public String getTypeCode() {
        return "T";
    }

    @Override
    public String toSaveDetail() {
        return "";
    }

    @Override
    public String toDisplayDetail() {
        return "";
    }
}
