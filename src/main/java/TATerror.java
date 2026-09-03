import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Scanner;

/**
 * Core logic of the "TA Terror" task-tracking chatbot: a sarcastic Duke-clone that
 * parses user commands (todo/deadline/event/list/mark/unmark/delete/find/bye),
 * mutates an in-memory task list, and persists it to {@link #DATA_FILE_PATH}.
 *
 * <p>This class is UI-agnostic - both the text CLI ({@link #main(String[])}) and the
 * JavaFX GUI ({@link MainWindow}) drive it purely through {@link #getResponse(String)}.
 */
public class TATerror {
    private static final String DATA_FILE_PATH = "." + File.separator + "data" + File.separator + "tasks.txt";
    private static final DateTimeFormatter DISPLAY_FORMAT = DateTimeFormatter.ofPattern("MMM dd yyyy");

    private String[] descriptions = new String[100];
    private boolean[] isDone = new boolean[100];
    private String[] types = new String[100];
    private String[] byRaw = new String[100];
    private String[] fromRaw = new String[100];
    private String[] toRaw = new String[100];
    private int taskCount;

    /**
     * Creates a new TA Terror instance, loading any previously saved tasks from
     * {@link #DATA_FILE_PATH} (starting with an empty list if none exists).
     */
    public TATerror() {
        taskCount = loadTasks();
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
            if (input.equals("bye")) {
                return "Bye. Try to disappoint someone else next time.";
            } else if (input.equals("list")) {
                response.append("Here are the tasks in your list:\n");
                for (int i = 0; i < taskCount; i++) {
                    response.append((i + 1) + "." + formatTask(i) + "\n");
                }
            } else if (input.startsWith("mark ") || input.startsWith("unmark ")) {
                boolean marking = input.startsWith("mark ");
                int numberStart = marking ? 5 : 7;
                int index = Integer.parseInt(input.substring(numberStart)) - 1;
                if (index < 0 || index >= taskCount) {
                    response.append("OOPS!!! That task number doesn't even exist. Try again.");
                } else {
                    isDone[index] = marking;
                    response.append(marking
                            ? "Nice! I've marked this task as done:\n"
                            : "OK, I've marked this task as not done yet:\n");
                    response.append("  " + formatTask(index));
                    saveTasks();
                }
            } else if (input.startsWith("delete ")) {
                int index = Integer.parseInt(input.substring(7)) - 1;
                if (index < 0 || index >= taskCount) {
                    response.append("OOPS!!! That task number doesn't even exist. Try again.");
                } else {
                    response.append("Noted. I've removed this task:\n");
                    response.append("  " + formatTask(index) + "\n");
                    for (int i = index; i < taskCount - 1; i++) {
                        descriptions[i] = descriptions[i + 1];
                        isDone[i] = isDone[i + 1];
                        types[i] = types[i + 1];
                        byRaw[i] = byRaw[i + 1];
                        fromRaw[i] = fromRaw[i + 1];
                        toRaw[i] = toRaw[i + 1];
                    }
                    taskCount--;
                    response.append("Now you have " + taskCount + " tasks in the list.");
                    saveTasks();
                }
            } else if (input.equals("todo") || input.startsWith("todo ")) {
                String description = input.length() > 4 ? input.substring(5).trim() : "";
                if (description.isEmpty()) {
                    response.append("OOPS!!! A todo needs an actual description. Use your words.");
                } else {
                    types[taskCount] = "T";
                    descriptions[taskCount] = description;
                    response.append(addTaskMessage(taskCount));
                    taskCount++;
                    saveTasks();
                }
            } else if (input.equals("deadline") || input.startsWith("deadline ")) {
                String rest = input.length() > 8 ? input.substring(9) : "";
                if (!rest.contains(" /by ")) {
                    response.append("OOPS!!! A deadline needs a description AND a '/by' date (e.g. 2019-10-15).");
                } else {
                    String[] parts = rest.split(" /by ");
                    types[taskCount] = "D";
                    descriptions[taskCount] = parts[0];
                    byRaw[taskCount] = parts[1];
                    response.append(addTaskMessage(taskCount));
                    taskCount++;
                    saveTasks();
                }
            } else if (input.equals("event") || input.startsWith("event ")) {
                String rest = input.length() > 5 ? input.substring(6) : "";
                if (!rest.contains(" /from ") || !rest.contains(" /to ")) {
                    response.append("OOPS!!! An event needs '/from' and '/to' details. Don't skip steps.");
                } else {
                    String[] fromSplit = rest.split(" /from ");
                    String[] toSplit = fromSplit[1].split(" /to ");
                    types[taskCount] = "E";
                    descriptions[taskCount] = fromSplit[0];
                    fromRaw[taskCount] = toSplit[0];
                    toRaw[taskCount] = toSplit[1];
                    response.append(addTaskMessage(taskCount));
                    taskCount++;
                    saveTasks();
                }
            } else if (input.equals("find") || input.startsWith("find ")) {
                String keyword = input.length() > 4 ? input.substring(5).trim() : "";
                if (keyword.isEmpty()) {
                    response.append("OOPS!!! Find what, exactly? Give me a keyword.");
                } else {
                    response.append("Here are the matching tasks in your list:\n");
                    int matchCount = 0;
                    for (int i = 0; i < taskCount; i++) {
                        if (descriptions[i].contains(keyword)) {
                            matchCount++;
                            response.append(matchCount + "." + formatTask(i) + "\n");
                        }
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
        String banner = " _____ _         _____                          \n"
                + "|_   _/ \\       |_   _|__ _ __ _ __ ___  _ __   \n"
                + "  | |/ _ \\        | |/ _ \\ '__| '__/ _ \\| '__|  \n"
                + "  | / ___ \\       | |  __/ |  | | | (_) | |     \n"
                + "  |_/_/   \\_\\     |_|\\___|_|  |_|  \\___/|_|     \n";
        System.out.println(banner);
        System.out.println("____________________________________________________________");
        System.out.println("Oh. It's you.");
        System.out.println("I'm TA Terror. Try not to waste my time.");
        System.out.println("____________________________________________________________");

        Scanner scanner = new Scanner(System.in);
        String input = scanner.nextLine();
        while (!input.equals("bye")) {
            System.out.println("____________________________________________________________");
            System.out.println(taTerror.getResponse(input));
            System.out.println("____________________________________________________________");
            input = scanner.nextLine();
        }
        System.out.println("____________________________________________________________");
        System.out.println(taTerror.getResponse("bye"));
        System.out.println("____________________________________________________________");
        scanner.close();
    }

    /**
     * Builds the standard "task added" confirmation message for the task at
     * {@code index}, including the updated task count.
     */
    private String addTaskMessage(int index) {
        return "Got it. I've added this task:\n  " + formatTask(index)
                + "\nNow you have " + (index + 1) + " tasks in the list.";
    }

    /**
     * Formats a raw deadline date string for display. If {@code raw} parses as an
     * ISO-8601 date (e.g. {@code 2019-10-15}), it's rendered as "MMM dd yyyy"
     * (e.g. "Oct 15 2019"); otherwise it's returned unchanged, so a freeform
     * date string typed by the user doesn't crash the display.
     */
    private String formatDeadlineBy(String raw) {
        try {
            LocalDate date = LocalDate.parse(raw.trim());
            return date.format(DISPLAY_FORMAT);
        } catch (DateTimeParseException e) {
            return raw;
        }
    }

    /**
     * Renders the task at {@code index} as a single display line, e.g.
     * {@code [T][X] read book} or {@code [D][ ] submit report (by: Sep 01 2026)}.
     */
    private String formatTask(int index) {
        String status = isDone[index] ? "[X]" : "[ ]";
        String base = "[" + types[index] + "]" + status + " " + descriptions[index];
        if (types[index].equals("D")) {
            return base + " (by: " + formatDeadlineBy(byRaw[index]) + ")";
        } else if (types[index].equals("E")) {
            return base + " (from: " + fromRaw[index] + " to: " + toRaw[index] + ")";
        }
        return base;
    }

    /**
     * Writes every current task to {@link #DATA_FILE_PATH} in the pipe-delimited
     * save format (creating the parent directory if needed). Failures are reported
     * to the user rather than thrown, since a save failure shouldn't crash the app.
     */
    private void saveTasks() {
        try {
            Path dataPath = Paths.get(DATA_FILE_PATH);
            Files.createDirectories(dataPath.getParent());
            FileWriter writer = new FileWriter(dataPath.toFile());
            for (int i = 0; i < taskCount; i++) {
                String doneFlag = isDone[i] ? "1" : "0";
                if (types[i].equals("D")) {
                    writer.write("D | " + doneFlag + " | " + descriptions[i] + " | " + byRaw[i] + "\n");
                } else if (types[i].equals("E")) {
                    writer.write("E | " + doneFlag + " | " + descriptions[i] + " | " + fromRaw[i]
                            + " | " + toRaw[i] + "\n");
                } else {
                    writer.write("T | " + doneFlag + " | " + descriptions[i] + "\n");
                }
            }
            writer.close();
        } catch (IOException e) {
            System.out.println("OOPS!!! Couldn't save your tasks. Don't blame me if you lose them.");
        }
    }

    /**
     * Reads tasks from {@link #DATA_FILE_PATH} into the in-memory arrays.
     *
     * @return the number of tasks loaded, or 0 if the save file doesn't exist yet
     *         or couldn't be read
     */
    private int loadTasks() {
        File dataFile = new File(DATA_FILE_PATH);
        if (!dataFile.exists()) {
            return 0;
        }
        int count = 0;
        try {
            Scanner fileScanner = new Scanner(dataFile);
            while (fileScanner.hasNextLine()) {
                String[] parts = fileScanner.nextLine().split(" \\| ");
                types[count] = parts[0];
                isDone[count] = parts[1].equals("1");
                descriptions[count] = parts[2];
                if (parts[0].equals("D")) {
                    byRaw[count] = parts[3];
                } else if (parts[0].equals("E")) {
                    fromRaw[count] = parts[3];
                    toRaw[count] = parts[4];
                }
                count++;
            }
            fileScanner.close();
        } catch (IOException e) {
            System.out.println("OOPS!!! Couldn't load your saved tasks. Starting fresh.");
            return 0;
        }
        return count;
    }
}