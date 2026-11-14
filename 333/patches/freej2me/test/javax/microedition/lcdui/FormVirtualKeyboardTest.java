package javax.microedition.lcdui;

import org.recompile.mobile.Mobile;
import org.recompile.mobile.MobilePlatform;

public final class FormVirtualKeyboardTest {
    private static int passed;
    private static Display display;

    public static void main(String[] args) {
        try {
            Mobile.setPlatform(new MobilePlatform(240, 320));
            display = new Display();
            run("OK opens only selected TextField", FormVirtualKeyboardTest::okOpensOnlySelectedTextField);
            run("Done commits and notifies once", FormVirtualKeyboardTest::doneCommitsAndNotifiesOnce);
            run("Cancel restores original", FormVirtualKeyboardTest::cancelRestoresOriginal);
            run("keyboard navigation does not move Form selection", FormVirtualKeyboardTest::keyboardNavigationDoesNotMoveFormSelection);
            run("uneditable field does not open", FormVirtualKeyboardTest::uneditableFieldDoesNotOpen);
            run("two fields remain isolated", FormVirtualKeyboardTest::twoFieldsRemainIsolated);
            run("key repeat does not duplicate activation", FormVirtualKeyboardTest::keyRepeatDoesNotDuplicateActivation);
            System.out.println("FormVirtualKeyboardTest: PASS (" + passed + " tests)");
            System.exit(0);
        } catch (Throwable failure) {
            failure.printStackTrace();
            System.exit(1);
        }
    }

    private static void okOpensOnlySelectedTextField() {
        TextField field = new TextField("Name", "", 16, TextField.ANY);
        Form form = show(new Form("Edit", new Item[] {
                new StringItem("Info", "not editable"), field
        }));
        form.keyPressed(Mobile.KEY_NUM5);
        assertFalse(form.isVirtualKeyboardOpen());
        form.keyPressed(Mobile.KEY_NUM8);
        form.keyPressed(Mobile.KEY_NUM5);
        assertTrue(form.isVirtualKeyboardOpen());
    }

    private static void doneCommitsAndNotifiesOnce() {
        TextField field = new TextField("Name", "", 16, TextField.ANY);
        CountingListener listener = new CountingListener();
        Form form = new Form("Edit", new Item[] { field });
        form.setItemStateListener(listener);
        show(form);
        openAndTypeQ(form);
        chooseDone(form);
        assertEquals("q", field.getString());
        assertEquals(1, listener.calls);
        assertSame(field, listener.lastItem);
        assertFalse(form.isVirtualKeyboardOpen());
    }

    private static void cancelRestoresOriginal() {
        TextField field = new TextField("Name", "old", 16, TextField.ANY);
        Form form = show(new Form("Edit", new Item[] { field }));
        openAndTypeQ(form);
        chooseCancel(form);
        assertEquals("old", field.getString());
        assertFalse(form.isVirtualKeyboardOpen());
    }

    private static void keyboardNavigationDoesNotMoveFormSelection() {
        TextField first = new TextField("First", "", 16, TextField.ANY);
        TextField second = new TextField("Second", "", 16, TextField.ANY);
        Form form = show(new Form("Edit", new Item[] { first, second }));
        form.keyPressed(Mobile.KEY_NUM5);
        form.keyPressed(Mobile.KEY_NUM8);
        assertEquals(0, form.currentItem);
        assertTrue(form.isVirtualKeyboardOpen());
    }

    private static void uneditableFieldDoesNotOpen() {
        TextField field = new TextField("Locked", "fixed", 16,
                TextField.ANY | TextField.UNEDITABLE);
        Form form = show(new Form("Edit", new Item[] { field }));
        form.keyPressed(Mobile.KEY_NUM5);
        assertFalse(form.isVirtualKeyboardOpen());
        assertEquals("fixed", field.getString());
    }

    private static void twoFieldsRemainIsolated() {
        TextField first = new TextField("First", "a", 16, TextField.ANY);
        TextField second = new TextField("Second", "b", 16, TextField.ANY);
        Form form = show(new Form("Edit", new Item[] { first, second }));
        openAndTypeQ(form);
        chooseDone(form);
        form.keyPressed(Mobile.KEY_NUM8);
        openAndTypeQ(form);
        chooseCancel(form);
        assertEquals("aq", first.getString());
        assertEquals("b", second.getString());
    }

    private static void keyRepeatDoesNotDuplicateActivation() {
        TextField field = new TextField("Name", "", 16, TextField.ANY);
        Form form = show(new Form("Edit", new Item[] { field }));
        form.keyPressed(Mobile.KEY_NUM5);
        form.keyPressed(Mobile.KEY_NUM5);
        form.keyRepeated(Mobile.KEY_NUM5);
        chooseDone(form);
        assertEquals("q", field.getString());
    }

    private static Form show(Form form) {
        display.setCurrent(form);
        return form;
    }

    private static void openAndTypeQ(Form form) {
        form.keyPressed(Mobile.KEY_NUM5);
        assertTrue(form.isVirtualKeyboardOpen());
        form.keyPressed(Mobile.KEY_NUM5);
    }

    private static void chooseDone(Form form) {
        form.keyPressed(Mobile.KEY_NUM2);
        form.keyPressed(Mobile.KEY_NUM6);
        form.keyPressed(Mobile.KEY_NUM6);
        form.keyPressed(Mobile.KEY_NUM6);
        form.keyPressed(Mobile.KEY_NUM5);
    }

    private static void chooseCancel(Form form) {
        form.keyPressed(Mobile.KEY_NUM2);
        form.keyPressed(Mobile.KEY_NUM6);
        form.keyPressed(Mobile.KEY_NUM6);
        form.keyPressed(Mobile.KEY_NUM6);
        form.keyPressed(Mobile.KEY_NUM6);
        form.keyPressed(Mobile.KEY_NUM5);
    }

    private static final class CountingListener implements ItemStateListener {
        private int calls;
        private Item lastItem;

        @Override
        public void itemStateChanged(Item item) {
            calls++;
            lastItem = item;
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
