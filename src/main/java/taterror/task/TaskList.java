package taterror.task;

import java.util.ArrayList;
import java.util.List;

/**
 * The in-memory list of tasks, and the operations to add, remove, look up, and
 * search them. Owns no persistence logic itself - see {@link Storage} for
 * loading/saving.
 */
public class TaskList {
    private final List<Task> tasks;

    /**
     * Creates an empty task list.
     */
    public TaskList() {
        this.tasks = new ArrayList<>();
    }

    /**
     * Creates a task list pre-populated with {@code initialTasks} (e.g. as loaded
     * by {@link Storage#load()}).
     */
    public TaskList(List<Task> initialTasks) {
        this.tasks = new ArrayList<>(initialTasks);
    }

    /**
     * Appends {@code task} to the end of the list.
     */
    public void add(Task task) {
        tasks.add(task);
    }

    /**
     * Removes and returns the task at {@code index}.
     */
    public Task remove(int index) {
        return tasks.remove(index);
    }

    public Task get(int index) {
        return tasks.get(index);
    }

    public int size() {
        return tasks.size();
    }

    /**
     * Returns whether {@code index} refers to an actual task in this list
     * (i.e. is safe to pass to {@link #get} or {@link #remove}).
     */
    public boolean isValidIndex(int index) {
        return index >= 0 && index < tasks.size();
    }

    /**
     * Returns a live, mutable view of all tasks, in list order. Intended for
     * {@link Storage#save}; callers elsewhere should prefer {@link #get}.
     */
    public List<Task> asList() {
        return tasks;
    }

    /**
     * Returns every task whose description contains {@code keyword}, in list
     * order.
     */
    public List<Task> findByKeyword(String keyword) {
        List<Task> matches = new ArrayList<>();
        for (Task task : tasks) {
            if (task.getDescription().contains(keyword)) {
                matches.add(task);
            }
        }
        return matches;
    }
}
