/**
 * Core logic of the "TA Terror" task-tracking chatbot: a sarcastic Duke-clone that
 * parses user commands (todo/deadline/event/list/mark/unmark/delete/find/bye),
 * mutates a {@link TaskList}, and persists it via {@link Storage}.
 *
 * <p>This class is UI-agnostic - both the text CLI ({@link #main(String[])}) and the
 * JavaFX GUI ({@link MainWindow}) drive it purely through {@link #getResponse(String)}.
 * Command recognition/extraction is delegated to {@link Parser}.
 */
public class TATerror {
    private static final String DATA_FILE_PATH = "." + java.io.File.separator + "data"
            + java.io.File.separator + "tasks.txt";

    private final Storage storage;
    private final TaskList tasks;

    /**
     * Creates a new TA Terror instance, loading any previously saved tasks from
     * the save file (starting with an empty list if none exists).
     */
    public TATerror() {
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
        StringBuilder response = new StringBuilder();
        try {
            if (Parser.isByeCommand(input)) {
                return "Bye. Try to disappoint someone else next time.";
            } else if (Parser.isListCommand(input)) {
                response.append("Here are the tasks in your list:\n");
                for (int i = 0; i < tasks.size(); i++) {
                    response.append((i + 1) + "." + tasks.get(i) + "\n");
                }
            } else if (Parser.isMarkCommand(input) || Parser.isUnmarkCommand(input)) {
                boolean marking = Parser.isMarkCommand(input);
                int index = Parser.parseTaskIndex(input, marking ? "mark" : "unmark");
                if (!tasks.isValidIndex(index)) {
                    response.append("OOPS!!! That task number doesn't even exist. Try again.");
                } else {
                    Task task = tasks.get(index);
                    if (marking) {
                        task.markAsDone();
                    } else {
                        task.markAsNotDone();
                    }
                    response.append(marking
                            ? "Nice! I've marked this task as done:\n"
                            : "OK, I've marked this task as not done yet:\n");
                    response.append("  " + task);
                    storage.save(tasks.asList());
                }
            } else if (Parser.isDeleteCommand(input)) {
                int index = Parser.parseTaskIndex(input, "delete");
                if (!tasks.isValidIndex(index)) {
                    response.append("OOPS!!! That task number doesn't even exist. Try again.");
                } else {
                    Task removed = tasks.remove(index);
                    response.append("Noted. I've removed this task:\n");
                    response.append("  " + removed + "\n");
                    response.append("Now you have " + tasks.size() + " tasks in the list.");
                    storage.save(tasks.asList());
                }
            } else if (Parser.isTodoCommand(input)) {
                String description = Parser.parseArguments(input, "todo").trim();
                if (description.isEmpty()) {
                    response.append("OOPS!!! A todo needs an actual description. Use your words.");
                } else {
                    Task todo = new Todo(description);
                    tasks.add(todo);
                    response.append(addTaskMessage(todo));
                    storage.save(tasks.asList());
                }
            } else if (Parser.isDeadlineCommand(input)) {
                String rest = Parser.parseArguments(input, "deadline");
                String[] parts = Parser.splitDeadlineArgs(rest);
                if (parts == null) {
                    response.append("OOPS!!! A deadline needs a description AND a '/by' date (e.g. 2019-10-15).");
                } else {
                    Task deadline = new Deadline(parts[0], parts[1]);
                    tasks.add(deadline);
                    response.append(addTaskMessage(deadline));
                    storage.save(tasks.asList());
                }
            } else if (Parser.isEventCommand(input)) {
                String rest = Parser.parseArguments(input, "event");
                String[] parts = Parser.splitEventArgs(rest);
                if (parts == null) {
                    response.append("OOPS!!! An event needs '/from' and '/to' details. Don't skip steps.");
                } else {
                    Task event = new Event(parts[0], parts[1], parts[2]);
                    tasks.add(event);
                    response.append(addTaskMessage(event));
                    storage.save(tasks.asList());
                }
            } else if (Parser.isFindCommand(input)) {
                String keyword = Parser.parseArguments(input, "find").trim();
                if (keyword.isEmpty()) {
                    response.append("OOPS!!! Find what, exactly? Give me a keyword.");
                } else {
                    response.append("Here are the matching tasks in your list:\n");
                    int matchCount = 0;
                    for (Task task : tasks.findByKeyword(keyword)) {
                        matchCount++;
                        response.append(matchCount + "." + task + "\n");
                    }
                    if (matchCount == 0) {
                        response.append("No matches. Shocking, I know.");
                    }
                }
            } else {
                response.append("OOPS!!! I have no idea what you just said. Try again, slower this time.");
            }
        } catch (NumberFormatException e) {
            response.append("OOPS!!! That's not even a number. Are you okay?");
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
        TATerror taTerror = new TATerror();
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
