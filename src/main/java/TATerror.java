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

public class TATerror {
    private static final String DATA_FILE_PATH = "." + File.separator + "data" + File.separator + "tasks.txt";
    private static final DateTimeFormatter DISPLAY_FORMAT = DateTimeFormatter.ofPattern("MMM dd yyyy");

    public static void main(String[] args) {
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

        String[] descriptions = new String[100];
        boolean[] isDone = new boolean[100];
        String[] types = new String[100];
        String[] byRaw = new String[100];
        String[] fromRaw = new String[100];
        String[] toRaw = new String[100];
        int taskCount = loadTasks(descriptions, isDone, types, byRaw, fromRaw, toRaw);

        Scanner scanner = new Scanner(System.in);
        String input = scanner.nextLine();

        while (!input.equals("bye")) {
            System.out.println("____________________________________________________________");
            try {
                if (input.equals("list")) {
                    System.out.println("Here are the tasks in your list:");
                    for (int i = 0; i < taskCount; i++) {
                        System.out.println((i + 1) + "."
                                + formatTask(i, descriptions, isDone, types, byRaw, fromRaw, toRaw));
                    }
                } else if (input.startsWith("mark ") || input.startsWith("unmark ")) {
                    boolean marking = input.startsWith("mark ");
                    int numberStart = marking ? 5 : 7;
                    int index = Integer.parseInt(input.substring(numberStart)) - 1;
                    if (index < 0 || index >= taskCount) {
                        System.out.println("OOPS!!! That task number doesn't even exist. Try again.");
                    } else {
                        isDone[index] = marking;
                        System.out.println(marking
                                ? "Nice! I've marked this task as done:"
                                : "OK, I've marked this task as not done yet:");
                        System.out.println("  " + formatTask(index, descriptions, isDone, types, byRaw, fromRaw, toRaw));
                        saveTasks(descriptions, isDone, types, byRaw, fromRaw, toRaw, taskCount);
                    }
                } else if (input.startsWith("delete ")) {
                    int index = Integer.parseInt(input.substring(7)) - 1;
                    if (index < 0 || index >= taskCount) {
                        System.out.println("OOPS!!! That task number doesn't even exist. Try again.");
                    } else {
                        System.out.println("Noted. I've removed this task:");
                        System.out.println("  " + formatTask(index, descriptions, isDone, types, byRaw, fromRaw, toRaw));
                        for (int i = index; i < taskCount - 1; i++) {
                            descriptions[i] = descriptions[i + 1];
                            isDone[i] = isDone[i + 1];
                            types[i] = types[i + 1];
                            byRaw[i] = byRaw[i + 1];
                            fromRaw[i] = fromRaw[i + 1];
                            toRaw[i] = toRaw[i + 1];
                        }
                        taskCount--;
                        System.out.println("Now you have " + taskCount + " tasks in the list.");
                        saveTasks(descriptions, isDone, types, byRaw, fromRaw, toRaw, taskCount);
                    }
                } else if (input.equals("todo") || input.startsWith("todo ")) {
                    String description = input.length() > 4 ? input.substring(5).trim() : "";
                    if (description.isEmpty()) {
                        System.out.println("OOPS!!! A todo needs an actual description. Use your words.");
                    } else {
                        types[taskCount] = "T";
                        descriptions[taskCount] = description;
                        addTask(descriptions, isDone, types, byRaw, fromRaw, toRaw, taskCount);
                        taskCount++;
                        saveTasks(descriptions, isDone, types, byRaw, fromRaw, toRaw, taskCount);
                    }
                } else if (input.equals("deadline") || input.startsWith("deadline ")) {
                    String rest = input.length() > 8 ? input.substring(9) : "";
                    if (!rest.contains(" /by ")) {
                        System.out.println("OOPS!!! A deadline needs a description AND a '/by' date (e.g. 2019-10-15).");
                    } else {
                        String[] parts = rest.split(" /by ");
                        types[taskCount] = "D";
                        descriptions[taskCount] = parts[0];
                        byRaw[taskCount] = parts[1];
                        addTask(descriptions, isDone, types, byRaw, fromRaw, toRaw, taskCount);
                        taskCount++;
                        saveTasks(descriptions, isDone, types, byRaw, fromRaw, toRaw, taskCount);
                    }
                } else if (input.equals("event") || input.startsWith("event ")) {
                    String rest = input.length() > 5 ? input.substring(6) : "";
                    if (!rest.contains(" /from ") || !rest.contains(" /to ")) {
                        System.out.println("OOPS!!! An event needs '/from' and '/to' details. Don't skip steps.");
                    } else {
                        String[] fromSplit = rest.split(" /from ");
                        String[] toSplit = fromSplit[1].split(" /to ");
                        types[taskCount] = "E";
                        descriptions[taskCount] = fromSplit[0];
                        fromRaw[taskCount] = toSplit[0];
                        toRaw[taskCount] = toSplit[1];
                        addTask(descriptions, isDone, types, byRaw, fromRaw, toRaw, taskCount);
                        taskCount++;
                        saveTasks(descriptions, isDone, types, byRaw, fromRaw, toRaw, taskCount);
                    }
                } else if (input.equals("find") || input.startsWith("find ")) {
                    String keyword = input.length() > 4 ? input.substring(5).trim() : "";
                    if (keyword.isEmpty()) {
                        System.out.println("OOPS!!! Find what, exactly? Give me a keyword.");
                    } else {
                        System.out.println("Here are the matching tasks in your list:");
                        int matchCount = 0;
                        for (int i = 0; i < taskCount; i++) {
                            if (descriptions[i].contains(keyword)) {
                                matchCount++;
                                System.out.println(matchCount + "."
                                        + formatTask(i, descriptions, isDone, types, byRaw, fromRaw, toRaw));
                            }
                        }
                        if (matchCount == 0) {
                            System.out.println("No matches. Shocking, I know.");
                        }
                    }
                } else {
                    System.out.println("OOPS!!! I have no idea what you just said. Try again, slower this time.");
                }
            } catch (NumberFormatException e) {
                System.out.println("OOPS!!! That's not even a number. Are you okay?");
            }
            System.out.println("____________________________________________________________");
            input = scanner.nextLine();
        }

        System.out.println("____________________________________________________________");
        System.out.println("Bye. Try to disappoint someone else next time.");
        System.out.println("____________________________________________________________");
        scanner.close();
    }

    private static void addTask(String[] descriptions, boolean[] isDone, String[] types,
                                String[] byRaw, String[] fromRaw, String[] toRaw, int index) {
        System.out.println("Got it. I've added this task:");
        System.out.println("  " + formatTask(index, descriptions, isDone, types, byRaw, fromRaw, toRaw));
        System.out.println("Now you have " + (index + 1) + " tasks in the list.");
    }

    private static String formatDeadlineBy(String raw) {
        try {
            LocalDate date = LocalDate.parse(raw.trim());
            return date.format(DISPLAY_FORMAT);
        } catch (DateTimeParseException e) {
            return raw;
        }
    }

    private static String formatTask(int index, String[] descriptions, boolean[] isDone,
                                     String[] types, String[] byRaw, String[] fromRaw, String[] toRaw) {
        String status = isDone[index] ? "[X]" : "[ ]";
        String base = "[" + types[index] + "]" + status + " " + descriptions[index];
        if (types[index].equals("D")) {
            return base + " (by: " + formatDeadlineBy(byRaw[index]) + ")";
        } else if (types[index].equals("E")) {
            return base + " (from: " + fromRaw[index] + " to: " + toRaw[index] + ")";
        }
        return base;
    }

    private static void saveTasks(String[] descriptions, boolean[] isDone, String[] types,
                                  String[] byRaw, String[] fromRaw, String[] toRaw, int taskCount) {
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

    private static int loadTasks(String[] descriptions, boolean[] isDone, String[] types,
                                 String[] byRaw, String[] fromRaw, String[] toRaw) {
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