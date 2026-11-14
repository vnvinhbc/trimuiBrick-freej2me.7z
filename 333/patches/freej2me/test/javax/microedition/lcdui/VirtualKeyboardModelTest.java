package javax.microedition.lcdui;

import java.util.List;

public final class VirtualKeyboardModelTest {
    private static int passed;

    public static void main(String[] args) {
        run("qwerty navigation wraps", VirtualKeyboardModelTest::qwertyNavigationWraps);
        run("uneven rows clamp selection", VirtualKeyboardModelTest::unevenRowsClampSelection);
        run("characters append", VirtualKeyboardModelTest::charactersAppend);
        run("backspace removes final character", VirtualKeyboardModelTest::backspaceRemovesFinalCharacter);
        run("space appends", VirtualKeyboardModelTest::spaceAppends);
        run("shift changes letter case", VirtualKeyboardModelTest::shiftChangesLetterCase);
        run("done commits", VirtualKeyboardModelTest::doneCommits);
        run("cancel restores original", VirtualKeyboardModelTest::cancelRestoresOriginal);
        run("null and empty values normalize", VirtualKeyboardModelTest::nullAndEmptyValuesNormalize);
        run("maximum length blocks append", VirtualKeyboardModelTest::maximumLengthBlocksAppend);
        run("numeric layout accepts digits only", VirtualKeyboardModelTest::numericLayoutAcceptsDigitsOnly);
        run("decimal layout permits one separator", VirtualKeyboardModelTest::decimalLayoutPermitsOneSeparator);
        run("phone layout exposes phone characters", VirtualKeyboardModelTest::phoneLayoutExposesPhoneCharacters);
        run("email layout exposes email characters", VirtualKeyboardModelTest::emailLayoutExposesEmailCharacters);
        run("url layout exposes url characters", VirtualKeyboardModelTest::urlLayoutExposesUrlCharacters);
        run("password display masks plaintext", VirtualKeyboardModelTest::passwordDisplayMasksPlaintext);
        run("uneditable model rejects mutation", VirtualKeyboardModelTest::uneditableModelRejectsMutation);
        System.out.println("VirtualKeyboardModelTest: PASS (" + passed + " tests)");
    }

    private static void qwertyNavigationWraps() {
        VirtualKeyboardModel model = model("", 32, TextField.ANY);
        assertEquals("q", selectedLabel(model));
        model.move(VirtualKeyboardModel.Direction.LEFT);
        assertEquals("p", selectedLabel(model));
        model.move(VirtualKeyboardModel.Direction.RIGHT);
        assertEquals("q", selectedLabel(model));
        model.move(VirtualKeyboardModel.Direction.UP);
        assertEquals("Space", selectedLabel(model));
        model.move(VirtualKeyboardModel.Direction.DOWN);
        assertEquals("q", selectedLabel(model));
    }

    private static void unevenRowsClampSelection() {
        VirtualKeyboardModel model = model("", 32, TextField.ANY);
        moveTo(model, 0, 9);
        model.move(VirtualKeyboardModel.Direction.DOWN);
        assertEquals(1, model.getSelectedRow());
        assertEquals(8, model.getSelectedColumn());
        assertEquals("l", selectedLabel(model));
    }

    private static void charactersAppend() {
        VirtualKeyboardModel model = model("a", 32, TextField.ANY);
        model.activate();
        assertEquals("aq", model.getDraft());
    }

    private static void backspaceRemovesFinalCharacter() {
        VirtualKeyboardModel model = model("ab", 32, TextField.ANY);
        selectAction(model, VirtualKeyboardModel.Action.BACKSPACE);
        model.activate();
        assertEquals("a", model.getDraft());
    }

    private static void spaceAppends() {
        VirtualKeyboardModel model = model("a", 32, TextField.ANY);
        selectAction(model, VirtualKeyboardModel.Action.SPACE);
        model.activate();
        assertEquals("a ", model.getDraft());
    }

    private static void shiftChangesLetterCase() {
        VirtualKeyboardModel model = model("", 32, TextField.ANY);
        selectAction(model, VirtualKeyboardModel.Action.SHIFT);
        model.activate();
        assertTrue(model.isShifted());
        moveTo(model, 0, 0);
        assertEquals("Q", selectedLabel(model));
        model.activate();
        assertEquals("Q", model.getDraft());
    }

    private static void doneCommits() {
        VirtualKeyboardModel model = model("", 32, TextField.ANY);
        selectAction(model, VirtualKeyboardModel.Action.DONE);
        assertEquals(VirtualKeyboardModel.Result.COMMIT, model.activate());
    }

    private static void cancelRestoresOriginal() {
        VirtualKeyboardModel model = model("original", 32, TextField.ANY);
        moveTo(model, 0, 0);
        model.activate();
        selectAction(model, VirtualKeyboardModel.Action.CANCEL);
        assertEquals(VirtualKeyboardModel.Result.CANCEL, model.activate());
        assertEquals("original", model.getDraft());
    }

    private static void nullAndEmptyValuesNormalize() {
        assertEquals("", model(null, 8, TextField.ANY).getDraft());
        assertEquals("", model("", 8, TextField.ANY).getDraft());
    }

    private static void maximumLengthBlocksAppend() {
        VirtualKeyboardModel model = model("ab", 2, TextField.ANY);
        model.activate();
        assertEquals("ab", model.getDraft());
    }

    private static void numericLayoutAcceptsDigitsOnly() {
        VirtualKeyboardModel model = model("", 8, TextField.NUMERIC);
        assertFalse(labels(model).contains("q"));
        assertTrue(labels(model).contains("1"));
        selectLabel(model, "1");
        model.activate();
        assertEquals("1", model.getDraft());
    }

    private static void decimalLayoutPermitsOneSeparator() {
        VirtualKeyboardModel model = model("1", 8, TextField.DECIMAL);
        selectLabel(model, ".");
        model.activate();
        model.activate();
        assertEquals("1.", model.getDraft());
    }

    private static void phoneLayoutExposesPhoneCharacters() {
        VirtualKeyboardModel model = model("", 16, TextField.PHONENUMBER);
        assertTrue(labels(model).contains("+"));
        assertTrue(labels(model).contains("#"));
        assertFalse(labels(model).contains("q"));
    }

    private static void emailLayoutExposesEmailCharacters() {
        VirtualKeyboardModel model = model("", 32, TextField.EMAILADDR);
        assertTrue(labels(model).contains("@"));
        assertTrue(labels(model).contains("."));
    }

    private static void urlLayoutExposesUrlCharacters() {
        VirtualKeyboardModel model = model("", 64, TextField.URL);
        assertTrue(labels(model).contains("/"));
        assertTrue(labels(model).contains(":"));
    }

    private static void passwordDisplayMasksPlaintext() {
        VirtualKeyboardModel model = model("secret", 16, TextField.ANY | TextField.PASSWORD);
        assertEquals("******", model.getDisplayValue());
        assertFalse(model.getDisplayValue().contains("secret"));
    }

    private static void uneditableModelRejectsMutation() {
        VirtualKeyboardModel model = model("locked", 16, TextField.ANY | TextField.UNEDITABLE);
        assertFalse(model.isEditable());
        assertEquals(VirtualKeyboardModel.Result.CONTINUE, model.activate());
        assertEquals("locked", model.getDraft());
    }

    private static VirtualKeyboardModel model(String value, int max, int constraints) {
        return new VirtualKeyboardModel(value, max, constraints);
    }

    private static String selectedLabel(VirtualKeyboardModel model) {
        return model.getRows().get(model.getSelectedRow()).get(model.getSelectedColumn()).getLabel();
    }

    private static String labels(VirtualKeyboardModel model) {
        StringBuilder labels = new StringBuilder();
        for (List<VirtualKeyboardModel.Key> row : model.getRows()) {
            for (VirtualKeyboardModel.Key key : row) {
                labels.append(key.getLabel());
            }
        }
        return labels.toString();
    }

    private static void selectLabel(VirtualKeyboardModel model, String label) {
        for (int row = 0; row < model.getRows().size(); row++) {
            for (int column = 0; column < model.getRows().get(row).size(); column++) {
                if (label.equals(model.getRows().get(row).get(column).getLabel())) {
                    moveTo(model, row, column);
                    return;
                }
            }
        }
        throw new AssertionError("Missing key label: " + label);
    }

    private static void selectAction(VirtualKeyboardModel model, VirtualKeyboardModel.Action action) {
        for (int row = 0; row < model.getRows().size(); row++) {
            for (int column = 0; column < model.getRows().get(row).size(); column++) {
                if (action == model.getRows().get(row).get(column).getAction()) {
                    moveTo(model, row, column);
                    return;
                }
            }
        }
        throw new AssertionError("Missing action: " + action);
    }

    private static void moveTo(VirtualKeyboardModel model, int row, int column) {
        while (model.getSelectedRow() != row) {
            model.move(VirtualKeyboardModel.Direction.DOWN);
        }
        while (model.getSelectedColumn() != column) {
            model.move(VirtualKeyboardModel.Direction.RIGHT);
        }
    }

    private static void run(String name, Runnable test) {
        try {
            test.run();
            passed++;
        } catch (Throwable failure) {
            throw new AssertionError(name + " failed", failure);
        }
    }

    private static void assertEquals(Object expected, Object actual) {
        if (expected == null ? actual != null : !expected.equals(actual)) {
            throw new AssertionError("expected <" + expected + "> but was <" + actual + ">");
        }
    }

    private static void assertTrue(boolean condition) {
        if (!condition) throw new AssertionError("expected true");
    }

    private static void assertFalse(boolean condition) {
        if (condition) throw new AssertionError("expected false");
    }
}
