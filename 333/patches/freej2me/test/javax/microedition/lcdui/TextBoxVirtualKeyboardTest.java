package javax.microedition.lcdui;

import org.recompile.mobile.Mobile;
import org.recompile.mobile.MobilePlatform;

public final class TextBoxVirtualKeyboardTest {
    private static int passed;
    private static Display display;

    public static void main(String[] args) {
        try {
            Mobile.setPlatform(new MobilePlatform(240, 320));
            display = new Display();
            run("opens automatically when current", TextBoxVirtualKeyboardTest::opensAutomaticallyWhenCurrent);
            run("Done commits initial value edit", TextBoxVirtualKeyboardTest::doneCommitsInitialValueEdit);
            run("Cancel restores original", TextBoxVirtualKeyboardTest::cancelRestoresOriginal);
            run("maximum length is respected", TextBoxVirtualKeyboardTest::maximumLengthIsRespected);
            run("commands work after Done", TextBoxVirtualKeyboardTest::commandsWorkAfterDone);
            run("repaint does not immediately reopen", TextBoxVirtualKeyboardTest::repaintDoesNotImmediatelyReopen);
            run("later display activation reopens", TextBoxVirtualKeyboardTest::laterDisplayActivationReopens);
            run("display targets remain isolated", TextBoxVirtualKeyboardTest::displayTargetsRemainIsolated);
            run("key repeat does not duplicate activation", TextBoxVirtualKeyboardTest::keyRepeatDoesNotDuplicateActivation);
            System.out.println("TextBoxVirtualKeyboardTest: PASS (" + passed + " tests)");
            System.exit(0);
        } catch (Throwable failure) {
            failure.printStackTrace();
            System.exit(1);
        }
    }

    private static void opensAutomaticallyWhenCurrent() {
        TextBox box = show(new TextBox("Name", "", 16, TextField.ANY));
        assertTrue(box.isVirtualKeyboardOpen());
    }

    private static void doneCommitsInitialValueEdit() {
        TextBox box = show(new TextBox("Name", "a", 16, TextField.ANY));
        box.keyPressed(Mobile.KEY_NUM5);
        chooseDone(box);
        assertEquals("aq", box.getString());
        assertFalse(box.isVirtualKeyboardOpen());
    }

    private static void cancelRestoresOriginal() {
        TextBox box = show(new TextBox("Name", "old", 16, TextField.ANY));
        box.keyPressed(Mobile.KEY_NUM5);
        chooseCancel(box);
        assertEquals("old", box.getString());
        assertFalse(box.isVirtualKeyboardOpen());
    }

    private static void maximumLengthIsRespected() {
        TextBox box = show(new TextBox("Name", "a", 1, TextField.ANY));
        box.keyPressed(Mobile.KEY_NUM5);
        chooseDone(box);
        assertEquals("a", box.getString());
    }

    private static void commandsWorkAfterDone() {
        TextBox box = new TextBox("Name", "", 16, TextField.ANY);
        Command save = new Command("Save", Command.OK, 1);
        CountingCommandListener listener = new CountingCommandListener();
        box.addCommand(save);
        box.setCommandListener(listener);
        show(box);
        chooseDone(box);
        box.keyPressed(Mobile.NOKIA_SOFT1);
        assertEquals(1, listener.calls);
        assertSame(save, listener.command);
        assertSame(box, listener.displayable);
    }

    private static void repaintDoesNotImmediatelyReopen() {
        TextBox box = show(new TextBox("Name", "", 16, TextField.ANY));
        chooseDone(box);
        box.render();
        assertFalse(box.isVirtualKeyboardOpen());
    }

    private static void laterDisplayActivationReopens() {
        TextBox box = show(new TextBox("Name", "", 16, TextField.ANY));
        chooseDone(box);
        display.setCurrent(new Form("Other"));
        display.setCurrent(box);
        assertTrue(box.isVirtualKeyboardOpen());
    }

    private static void displayTargetsRemainIsolated() {
        TextBox first = show(new TextBox("First", "a", 16, TextField.ANY));
        first.keyPressed(Mobile.KEY_NUM5);
        TextBox second = show(new TextBox("Second", "b", 16, TextField.ANY));
        second.keyPressed(Mobile.KEY_NUM5);
        chooseDone(second);
        assertEquals("a", first.getString());
        assertEquals("bq", second.getString());
        display.setCurrent(first);
        chooseCancel(first);
        assertEquals("a", first.getString());
    }

    private static void keyRepeatDoesNotDuplicateActivation() {
        TextBox box = show(new TextBox("Name", "", 16, TextField.ANY));
        box.keyPressed(Mobile.KEY_NUM5);
        box.keyRepeated(Mobile.KEY_NUM5);
        chooseDone(box);
        assertEquals("q", box.getString());
    }

    private static TextBox show(TextBox box) {
        display.setCurrent(box);
        return box;
    }

    private static void chooseDone(TextBox box) {
        box.keyPressed(Mobile.KEY_NUM2);
        box.keyPressed(Mobile.KEY_NUM6);
        box.keyPressed(Mobile.KEY_NUM6);
        box.keyPressed(Mobile.KEY_NUM6);
        box.keyPressed(Mobile.KEY_NUM5);
    }

    private static void chooseCancel(TextBox box) {
        box.keyPressed(Mobile.KEY_NUM2);
        box.keyPressed(Mobile.KEY_NUM6);
        box.keyPressed(Mobile.KEY_NUM6);
        box.keyPressed(Mobile.KEY_NUM6);
        box.keyPressed(Mobile.KEY_NUM6);
        box.keyPressed(Mobile.KEY_NUM5);
    }

    private static final class CountingCommandListener implements CommandListener {
        private int calls;
        private Command command;
        private Displayable displayable;

        @Override
        public void commandAction(Command command, Displayable displayable) {
            calls++;
            this.command = command;
            this.displayable = displayable;
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

    private static void assertSame(Object expected, Object actual) {
        if (expected != actual) throw new AssertionError("expected same object");
    }

    private static void assertTrue(boolean condition) {
        if (!condition) throw new AssertionError("expected true");
    }

    private static void assertFalse(boolean condition) {
        if (condition) throw new AssertionError("expected false");
    }
}
