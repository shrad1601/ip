package taterror;

import java.io.File;
import java.util.List;

import taterror.parser.Parser;
import taterror.storage.Storage;
import taterror.task.Deadline;
import taterror.task.Event;
import taterror.task.Task;
import taterror.task.TaskList;
import taterror.task.Todo;
import taterror.ui.Ui;

/**
 * Core logic of the "TA Terror" task-tracking chatbot: a sarcastic Duke-clone that
 * parses user commands (todo/deadline/event/list/mark/unmark/delete/find/bye),
 * mutates a {@link TaskList}, and persists it via {@link Storage}.
 *
 * <p>This class is UI-agnostic - both the text CLI ({@link #main(String[])}) and the
 * JavaFX GUI ({@link taterror.gui.MainWindow}) drive it purely through
 * {@link #getResponse(String)}. Command recognition/extraction is delegated to
 * {@link Parser}.
 */
public class TaTerror {
    private static final String DATA_FILE_PATH = "." + File.separator + "data" + File.separator + "tasks.txt";

    private final Storage storage;
    private final TaskList tasks;

    /**
     * Creates a new TA Terror instance, loading any previously saved tasks from
     * the save file (starting with an empty list if none exists).
     */
    public TaTerror() {
        storage = new Storage(DATA_FILE_PATH);
        tasks = new TaskList(storage.load());
    }

    /**
     * Parses one line of user input, applies its effect to the task list (if any),
     * and returns the chatbot's reply text.
     *
     * <p>Supported commands: {@code bye}, {@code list}, {@code mark <n>},
     * {@code unmark <n>}, {@code delete <n>}, {@code todo <description>},
     * {@code deadline <description> /by <date>},
     * {@code event <description> /from <start> /to <end>}, and
     * {@code find <keyword>}. Anything else, or a malformed index for
     * mark/unmark/delete, produces an error reply rather than throwing.
     *
     * @param input the raw command line typed by the user
     * @return the chatbot's reply, ready to display as-is
     */
    public String getResponse(String input) {
        try {
            switch (Parser.parseCommandType(input)) {
                case BYE:
                    return "Bye. Try to disappoint someone else next time.";
                case LIST:
                    return handleList();
                case MARK:
                case UNMARK:
                    return handleMarkOrUnmark(input);
                case DELETE:
                    return handleDelete(input);
                case TODO:
                    return handleTodo(input);
                case DEADLINE:
                    return handleDeadline(input);
                case EVENT:
                    return handleEvent(input);
                case FIND:
                    return handleFind(input);
                case UNKNOWN:
                default:
                    return "OOPS!!! I have no idea what you just said. Try again, slower this time.";
            }
        } catch (NumberFormatException e) {
            return "OOPS!!! That's not even a number. Are you okay?";
        }
    }

    /**
     * Handles the {@code list} command: every task, numbered from 1.
     */
    private String handleList() {
        StringBuilder response = new StringBuilder("Here are the tasks in your list:\n");
        for (int i = 0; i < tasks.size(); i++) {
            response.append((i + 1) + "." + tasks.get(i) + "\n");
        }
        return response.toString().trim();
    }

    /**
     * Handles a {@code mark}/{@code unmark} command: flips the target task's
     * done state and persists the change.
     */
    private String handleMarkOrUnmark(String input) {
        boolean marking = Parser.isMarkCommand(input);
        int markIndex = Parser.parseTaskIndex(input, marking ? "mark" : "unmark");
        if (!tasks.isValidIndex(markIndex)) {
            return "OOPS!!! That task number doesn't even exist. Try again.";
        }
        Task task = tasks.get(markIndex);
        if (marking) {
            task.markAsDone();
        } else {
            task.markAsNotDone();
        }
        storage.save(tasks.asList());
        String verb = marking
                ? "Nice! I've marked this task as done:\n"
                : "OK, I've marked this task as not done yet:\n";
        return verb + "  " + task;
    }

    /**
     * Handles a {@code delete} command: removes the target task and persists
     * the change.
     */
    private String handleDelete(String input) {
        int deleteIndex = Parser.parseTaskIndex(input, "delete");
        if (!tasks.isValidIndex(deleteIndex)) {
            return "OOPS!!! That task number doesn't even exist. Try again.";
        }
        Task removed = tasks.remove(deleteIndex);
        storage.save(tasks.asList());
        return "Noted. I've removed this task:\n  " + removed
                + "\nNow you have " + tasks.size() + " tasks in the list.";
    }

    /**
     * Handles a {@code todo} command: adds the task if a description was
     * given, and persists the change.
     */
    private String handleTodo(String input) {
        String description = Parser.parseArguments(input, "todo").trim();
        if (description.isEmpty()) {
            return "OOPS!!! A todo needs an actual description. Use your words.";
        }
        Task todo = new Todo(description);
        tasks.add(todo);
        storage.save(tasks.asList());
        return addTaskMessage(todo);
    }

    /**
     * Handles a {@code deadline} command: adds the task if it has both a
     * description and a {@code /by} date, and persists the change.
     */
    private String handleDeadline(String input) {
        String deadlineRest = Parser.parseArguments(input, "deadline");
        String[] deadlineParts = Parser.splitDeadlineArgs(deadlineRest);
        if (deadlineParts == null) {
            return "OOPS!!! A deadline needs a description AND a '/by' date (e.g. 2019-10-15).";
        }
        Task deadline = new Deadline(deadlineParts[0], deadlineParts[1]);
        tasks.add(deadline);
        storage.save(tasks.asList());
        return addTaskMessage(deadline);
    }

    /**
     * Handles an {@code event} command: adds the task if it has a
     * description, a {@code /from}, and a {@code /to}, and persists the
     * change.
     */
    private String handleEvent(String input) {
        String eventRest = Parser.parseArguments(input, "event");
        String[] eventParts = Parser.splitEventArgs(eventRest);
        if (eventParts == null) {
            return "OOPS!!! An event needs '/from' and '/to' details. Don't skip steps.";
        }
        Task event = new Event(eventParts[0], eventParts[1], eventParts[2]);
        tasks.add(event);
        storage.save(tasks.asList());
        return addTaskMessage(event);
    }

    /**
     * Handles a {@code find} command: every task whose description contains
     * the given keyword, numbered from 1.
     */
    private String handleFind(String input) {
        String keyword = Parser.parseArguments(input, "find").trim();
        if (keyword.isEmpty()) {
            return "OOPS!!! Find what, exactly? Give me a keyword.";
        }
        List<Task> matches = tasks.findByKeyword(keyword);
        if (matches.isEmpty()) {
            return "Here are the matching tasks in your list:\nNo matches. Shocking, I know.";
        }
        StringBuilder response = new StringBuilder("Here are the matching tasks in your list:\n");
        for (int i = 0; i < matches.size(); i++) {
            response.append((i + 1) + "." + matches.get(i) + "\n");
        }
        return response.toString().trim();
    }

    /**
     * Runs TA Terror as a text-based CLI: prints the greeting banner, then reads
     * commands from standard input and prints each reply until {@code bye}.
     *
     * @param args unused
     */
    public static void main(String[] args) {
        TaTerror taTerror = new TaTerror();
        Ui ui = new Ui();
        ui.showGreeting();

        String input = ui.readCommand();
        while (!input.equals("bye")) {
            ui.showResponse(taTerror.getResponse(input));
            input = ui.readCommand();
        }
        ui.showResponse(taTerror.getResponse("bye"));
        ui.close();
    }

    /**
     * Builds the standard "task added" confirmation message for {@code task},
     * including the updated task count.
     */
    private String addTaskMessage(Task task) {
        return "Got it. I've added this task:\n  " + task
                + "\nNow you have " + tasks.size() + " tasks in the list.";
    }
}
