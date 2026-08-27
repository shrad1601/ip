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
        int taskCount = 0;

        Scanner scanner = new Scanner(System.in);
        String input = scanner.nextLine();

        while (!input.equals("bye")) {
            System.out.println("____________________________________________________________");
            if (input.equals("list")) {
                for (int i = 0; i < taskCount; i++) {
                    System.out.println((i + 1) + ". " + tasks[i]);
                }
            } else {
                tasks[taskCount] = input;
                taskCount++;
                System.out.println("added: " + input);
            }
            System.out.println("____________________________________________________________");
            input = scanner.nextLine();
        }

        System.out.println("____________________________________________________________");
        System.out.println("Bye. Try to disappoint someone else next time.");
        System.out.println("____________________________________________________________");
        scanner.close();
    }
}