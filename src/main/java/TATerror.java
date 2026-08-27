import java.util.Scanner;

public class TATerror {
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

        String[] tasks = new String[100];
        boolean[] isDone = new boolean[100];
        String[] types = new String[100];
        String[] extraInfo = new String[100];
        int taskCount = 0;

        Scanner scanner = new Scanner(System.in);
        String input = scanner.nextLine();

        while (!input.equals("bye")) {
            System.out.println("____________________________________________________________");
            try {
                if (input.equals("list")) {
                    System.out.println("Here are the tasks in your list:");
                    for (int i = 0; i < taskCount; i++) {
                        System.out.println((i + 1) + "." + formatTask(i, tasks, isDone, types, extraInfo));
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
                        System.out.println("  " + formatTask(index, tasks, isDone, types, extraInfo));
                    }
                } else if (input.startsWith("delete ")) {
                    int index = Integer.parseInt(input.substring(7)) - 1;
                    if (index < 0 || index >= taskCount) {
                        System.out.println("OOPS!!! That task number doesn't even exist. Try again.");
                    } else {
                        System.out.println("Noted. I've removed this task:");
                        System.out.println("  " + formatTask(index, tasks, isDone, types, extraInfo));
                        for (int i = index; i < taskCount - 1; i++) {
                            tasks[i] = tasks[i + 1];
                            isDone[i] = isDone[i + 1];
                            types[i] = types[i + 1];
                            extraInfo[i] = extraInfo[i + 1];
                        }
                        taskCount--;
                        System.out.println("Now you have " + taskCount + " tasks in the list.");
                    }
                } else if (input.equals("todo") || input.startsWith("todo ")) {
                    String description = input.length() > 4 ? input.substring(5).trim() : "";
                    if (description.isEmpty()) {
                        System.out.println("OOPS!!! A todo needs an actual description. Use your words.");
                    } else {
                        types[taskCount] = "T";
                        tasks[taskCount] = description;
                        extraInfo[taskCount] = "";
                        addTask(tasks, isDone, types, extraInfo, taskCount);
                        taskCount++;
                    }
                } else if (input.equals("deadline") || input.startsWith("deadline ")) {
                    String rest = input.length() > 8 ? input.substring(9) : "";
                    if (!rest.contains(" /by ")) {
                        System.out.println("OOPS!!! A deadline needs a description AND a '/by' date.");
                    } else {
                        String[] parts = rest.split(" /by ");
                        types[taskCount] = "D";
                        tasks[taskCount] = parts[0];
                        extraInfo[taskCount] = "by: " + parts[1];
                        addTask(tasks, isDone, types, extraInfo, taskCount);
                        taskCount++;
                    }
                } else if (input.equals("event") || input.startsWith("event ")) {
                    String rest = input.length() > 5 ? input.substring(6) : "";
                    if (!rest.contains(" /from ") || !rest.contains(" /to ")) {
                        System.out.println("OOPS!!! An event needs '/from' and '/to' details. Don't skip steps.");
                    } else {
                        String[] fromSplit = rest.split(" /from ");
                        String[] toSplit = fromSplit[1].split(" /to ");
                        types[taskCount] = "E";
                        tasks[taskCount] = fromSplit[0];
                        extraInfo[taskCount] = "from: " + toSplit[0] + " to: " + toSplit[1];
                        addTask(tasks, isDone, types, extraInfo, taskCount);
                        taskCount++;
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

    private static void addTask(String[] tasks, boolean[] isDone, String[] types,
                                String[] extraInfo, int index) {
        System.out.println("Got it. I've added this task:");
        System.out.println("  " + formatTask(index, tasks, isDone, types, extraInfo));
        System.out.println("Now you have " + (index + 1) + " tasks in the list.");
    }

    private static String formatTask(int index, String[] tasks, boolean[] isDone,
                                     String[] types, String[] extraInfo) {
        String status = isDone[index] ? "[X]" : "[ ]";
        String extra = extraInfo[index].isEmpty() ? "" : " (" + extraInfo[index] + ")";
        return "[" + types[index] + "]" + status + " " + tasks[index] + extra;
    }
}