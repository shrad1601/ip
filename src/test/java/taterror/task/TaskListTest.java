package taterror.task;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

/**
 * Tests for {@link TaskList}, including its varargs {@link TaskList#addAll}.
 */
public class TaskListTest {

    @Test
    public void addAll_noTasks_sizeUnchanged() {
        TaskList tasks = new TaskList();
        tasks.addAll();
        assertEquals(0, tasks.size());
    }

    @Test
    public void addAll_multipleTasks_allAddedInOrder() {
        TaskList tasks = new TaskList();
        Todo first = new Todo("read book");
        Todo second = new Todo("sleep");

        tasks.addAll(first, second);

        assertEquals(2, tasks.size());
        assertEquals(first, tasks.get(0));
        assertEquals(second, tasks.get(1));
    }

    @Test
    public void isValidIndex_indexWithinBounds_returnsTrue() {
        TaskList tasks = new TaskList();
        tasks.add(new Todo("read book"));

        assertTrue(tasks.isValidIndex(0));
    }

    @Test
    public void isValidIndex_indexOutOfBounds_returnsFalse() {
        TaskList tasks = new TaskList();
        tasks.add(new Todo("read book"));

        assertFalse(tasks.isValidIndex(1));
        assertFalse(tasks.isValidIndex(-1));
    }

    @Test
    public void findByKeyword_matchingDescription_returnsMatch() {
        TaskList tasks = new TaskList();
        tasks.addAll(new Todo("read book"), new Todo("sleep"));

        assertEquals(1, tasks.findByKeyword("book").size());
    }
}
