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
            if (input.equals("list")) {
                for (int i = 0; i < taskCount; i++) {
                    System.out.println((i + 1) + "." + formatTask(i, tasks, isDone, types, extraInfo));
                }
            } else if (input.startsWith("mark ")) {
                int index = Integer.parseInt(input.substring(5)) - 1;
                isDone[index] = true;
                System.out.println("Nice! I've marked this task as done:");
                System.out.println("  " + formatTask(index, tasks, isDone, types, extraInfo));
            } else if (input.startsWith("unmark ")) {
                int index = Integer.parseInt(input.substring(7)) - 1;
                isDone[index] = false;
                System.out.println("OK, I've marked this task as not done yet:");
                System.out.println("  " + formatTask(index, tasks, isDone, types, extraInfo));
            } else if (input.startsWith("todo ")) {
                types[taskCount] = "T";
                tasks[taskCount] = input.substring(5);
                extraInfo[taskCount] = "";
                addTask(tasks, isDone, types, extraInfo, taskCount);
                taskCount++;
            } else if (input.startsWith("deadline ")) {
                String rest = input.substring(9);
                String[] parts = rest.split(" /by ");
                types[taskCount] = "D";
                tasks[taskCount] = parts[0];
                extraInfo[taskCount] = "by: " + parts[1];
                addTask(tasks, isDone, types, extraInfo, taskCount);
                taskCount++;
            } else if (input.startsWith("event ")) {
                String rest = input.substring(6);
                String[] fromSplit = rest.split(" /from ");
                String description = fromSplit[0];
                String[] toSplit = fromSplit[1].split(" /to ");
                types[taskCount] = "E";
                tasks[taskCount] = description;
                extraInfo[taskCount] = "from: " + toSplit[0] + " to: " + toSplit[1];
                addTask(tasks, isDone, types, extraInfo, taskCount);
                taskCount++;
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