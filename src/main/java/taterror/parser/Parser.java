package taterror.parser;

/**
 * Recognizes and extracts structured data from raw user command lines.
 * Stateless - all methods are static.
 */
public class Parser {
    private Parser() {
    }

    public static boolean isByeCommand(String input) {
        return input.equals("bye");
    }

    public static boolean isListCommand(String input) {
        return input.equals("list");
    }

    public static boolean isMarkCommand(String input) {
        return input.startsWith("mark ");
    }

    public static boolean isUnmarkCommand(String input) {
        return input.startsWith("unmark ");
    }

    public static boolean isDeleteCommand(String input) {
        return input.startsWith("delete ");
    }

    public static boolean isTodoCommand(String input) {
        return input.equals("todo") || input.startsWith("todo ");
    }

    public static boolean isDeadlineCommand(String input) {
        return input.equals("deadline") || input.startsWith("deadline ");
    }

    public static boolean isEventCommand(String input) {
        return input.equals("event") || input.startsWith("event ");
    }

    public static boolean isFindCommand(String input) {
        return input.equals("find") || input.startsWith("find ");
    }

    /**
     * Extracts the 1-based task index from a {@code mark}/{@code unmark}/
     * {@code delete} command and converts it to 0-based.
     *
     * @param input   the full command line, e.g. {@code "mark 2"}
     * @param keyword the command keyword the input starts with, e.g. {@code "mark"}
     * @return the 0-based index
     * @throws NumberFormatException if the text after the keyword isn't an integer
     */
    public static int parseTaskIndex(String input, String keyword) {
        return Integer.parseInt(input.substring(keyword.length() + 1)) - 1;
    }

    /**
     * Extracts everything after {@code keyword} and its following space,
     * un-trimmed. Returns {@code ""} if there's nothing after the keyword
     * (whether the input is the bare keyword, or the keyword plus one trailing
     * space with no further content).
     */
    public static String parseArguments(String input, String keyword) {
        return input.length() > keyword.length() ? input.substring(keyword.length() + 1) : "";
    }

    /**
     * Splits a deadline command's arguments (i.e. already past "deadline ")
     * into {@code [description, by]} on the required {@code " /by "} separator.
     *
     * @return the split fields, or {@code null} if {@code " /by "} is missing
     */
    public static String[] splitDeadlineArgs(String arguments) {
        if (!arguments.contains(" /by ")) {
            return null;
        }
        return arguments.split(" /by ");
    }

    /**
     * Splits an event command's arguments (i.e. already past "event ") into
     * {@code [description, from, to]} on the required {@code " /from "} and
     * {@code " /to "} separators.
     *
     * @return the split fields, or {@code null} if either separator is missing
     */
    public static String[] splitEventArgs(String arguments) {
        if (!arguments.contains(" /from ") || !arguments.contains(" /to ")) {
            return null;
        }
        String[] fromSplit = arguments.split(" /from ");
        String[] toSplit = fromSplit[1].split(" /to ");
        return new String[] {fromSplit[0], toSplit[0], toSplit[1]};
    }
}
