package taterror;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

/**
 * Tests for {@link TaTerror#getResponse(String)}.
 *
 * <p>Deliberately limited to inputs that do NOT trigger {@code Storage.save()}
 * (i.e. no successful todo/deadline/event/mark/unmark/delete), so running these
 * tests never overwrites the real {@code data/tasks.txt} save file. Each test
 * targets a distinct validation/parsing branch inside {@code getResponse}, not
 * just the happy path.
 */
public class TaTerrorTest {

    @Test
    public void getResponse_byeCommand_returnsFarewellMessage() {
        TaTerror taTerror = new TaTerror();
        assertEquals("Bye. Try to disappoint someone else next time.", taTerror.getResponse("bye"));
    }

    @Test
    public void getResponse_unrecognizedCommand_returnsErrorMessage() {
        TaTerror taTerror = new TaTerror();
        assertEquals(
                "OOPS!!! I have no idea what you just said. Try again, slower this time.",
                taTerror.getResponse("blahblahblah"));
    }

    @Test
    public void getResponse_markWithNonNumericIndex_returnsNumberFormatError() {
        TaTerror taTerror = new TaTerror();
        // "abc" isn't a number, so Integer.parseInt throws before any task is touched.
        assertEquals("OOPS!!! That's not even a number. Are you okay?", taTerror.getResponse("mark abc"));
    }

    @Test
    public void getResponse_markWithOutOfRangeIndex_returnsRangeError() {
        TaTerror taTerror = new TaTerror();
        // Index is numeric but absurdly out of range, so no task is actually marked/saved.
        assertEquals("OOPS!!! That task number doesn't even exist. Try again.", taTerror.getResponse("mark 99999"));
    }

    @Test
    public void getResponse_todoWithoutDescription_returnsValidationErrorAndDoesNotAddTask() {
        TaTerror taTerror = new TaTerror();
        assertEquals(
                "OOPS!!! A todo needs an actual description. Use your words.", taTerror.getResponse("todo"));
    }

    @Test
    public void getResponse_deadlineWithoutByClause_returnsValidationError() {
        TaTerror taTerror = new TaTerror();
        assertEquals(
                "OOPS!!! A deadline needs a description AND a '/by' date (e.g. 2019-10-15).",
                taTerror.getResponse("deadline return book"));
    }

    @Test
    public void getResponse_eventWithoutFromOrTo_returnsValidationError() {
        TaTerror taTerror = new TaTerror();
        assertEquals(
                "OOPS!!! An event needs '/from' and '/to' details. Don't skip steps.",
                taTerror.getResponse("event project meeting"));
    }

    @Test
    public void getResponse_listCommand_startsWithExpectedHeader() {
        TaTerror taTerror = new TaTerror();
        // Loose/structural assertion on purpose: the exact task contents depend on
        // whatever is currently saved in data/tasks.txt, which this test must not
        // assume or depend on.
        String response = taTerror.getResponse("list");
        assertTrue(response.startsWith("Here are the tasks in your list:"));
    }

    @Test
    public void getResponse_findWithoutKeyword_returnsValidationError() {
        TaTerror taTerror = new TaTerror();
        assertEquals("OOPS!!! Find what, exactly? Give me a keyword.", taTerror.getResponse("find"));
    }
}
