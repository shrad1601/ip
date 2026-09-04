package taterror.parser;

/**
 * Recognizes and extracts structured data from raw user command lines.
 * Stateless - all methods are static.
 */
public class Parser {
    private Parser() {
    }

    /**
     * Returns whether {@code input} is the {@code bye} command.
     */
    public static boolean isByeCommand(String input) {
        return input.equals("bye");
    }

    /**
     * Returns whether {@code input} is the {@code list} command.
     */
    public static boolean isListCommand(String input) {
        return input.equals("list");
    }

    /**
     * Returns whether {@code input} is a {@code mark} command.
     */
    public static boolean isMarkCommand(String input) {
        return input.startsWith("mark ");
    }

    /**
     * Returns whether {@code input} is an {@code unmark} command.
     */
    public static boolean isUnmarkCommand(String input) {
        return input.startsWith("unmark ");
    }

    /**
     * Returns whether {@code input} is a {@code delete} command.
     */
    public static boolean isDeleteCommand(String input) {
        return input.startsWith("delete ");
    }

    /**
     * Returns whether {@code input} is a {@code todo} command (with or
     * without a description).
     */
    public static boolean isTodoCommand(String input) {
        return input.equals("todo") || input.startsWith("todo ");
    }

    /**
     * Returns whether {@code input} is a {@code deadline} command (with or
     * without arguments).
     */
    public static boolean isDeadlineCommand(String input) {
        return input.equals("deadline") || input.startsWith("deadline ");
    }

    /**
     * Returns whether {@code input} is an {@code event} command (with or
     * without arguments).
     */
    public static boolean isEventCommand(String input) {
        return input.equals("event") || input.startsWith("event ");
    }

    /**
     * Returns whether {@code input} is a {@code find} command (with or
     * without a keyword).
     */
    public static boolean isFindCommand(String input) {
        return input.equals("find") || input.startsWith("find ");
    }

    /**
     * Returns the {@link CommandType} that {@code input} represents, or
     * {@link CommandType#UNKNOWN} if it doesn't match any recognized command.
     */
    public static CommandType parseCommandType(String input) {
        if (isByeCommand(input)) {
            return CommandType.BYE;
        } else if (isListCommand(input)) {
            return CommandType.LIST;
        } else if (isMarkCommand(input)) {
            return CommandType.MARK;
        } else if (isUnmarkCommand(input)) {
            return CommandType.UNMARK;
        } else if (isDeleteCommand(input)) {
            return CommandType.DELETE;
        } else if (isTodoCommand(input)) {
            return CommandType.TODO;
        } else if (isDeadlineCommand(input)) {
            return CommandType.DEADLINE;
        } else if (isEventCommand(input)) {
            return CommandType.EVENT;
        } else if (isFindCommand(input)) {
            return CommandType.FIND;
        } else {
            return CommandType.UNKNOWN;
        }
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
     *
     * @param input   the full command line, e.g. {@code "todo read book"}
     * @param keyword the command keyword the input starts with, e.g. {@code "todo"}
     * @return everything after the keyword, or {@code ""} if there's nothing there
     */
    public static String parseArguments(String input, String keyword) {
        return input.length() > keyword.length() ? input.substring(keyword.length() + 1) : "";
    }

    /**
     * Splits a deadline command's arguments (i.e. already past "deadline ")
     * into {@code [description, by]} on the required {@code " /by "} separator.
     *
     * @param arguments the deadline command's arguments, e.g.
     *                  {@code "submit report /by 2019-10-15"}
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
     * @param arguments the event command's arguments, e.g.
     *                  {@code "meeting /from 2pm /to 4pm"}
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
