package javax.microedition.lcdui;

import javax.microedition.midlet.MIDlet;
import javax.microedition.midlet.MIDletStateChangeException;

public final class VirtualKeyboardSmokeMidlet extends MIDlet implements CommandListener {
    private final Command next = new Command("TextBox", Command.OK, 1);
    private final Command back = new Command("Form", Command.BACK, 1);
    private final Command exit = new Command("Exit", Command.EXIT, 2);
    private Form form;
    private TextBox textBox;

    @Override
    protected void startApp() throws MIDletStateChangeException {
        if (form == null) {
            form = new Form("Keyboard Smoke", new Item[] {
                    new TextField("Text", "hello", 32, TextField.ANY),
                    new TextField("Number", "12", 10, TextField.NUMERIC),
                    new TextField("Password", "secret", 16, TextField.ANY | TextField.PASSWORD),
                    new TextField("Locked", "fixed", 16, TextField.ANY | TextField.UNEDITABLE)
            });
            form.addCommand(next);
            form.addCommand(exit);
            form.setCommandListener(this);

            textBox = new TextBox("TextBox", "edit me", 48, TextField.ANY);
            textBox.addCommand(back);
            textBox.addCommand(exit);
            textBox.setCommandListener(this);
        }
        getDisplay().setCurrent(form);
    }

    @Override
    public void commandAction(Command command, Displayable displayable) {
        if (command == next) {
            getDisplay().setCurrent(textBox);
        } else if (command == back) {
            getDisplay().setCurrent(form);
        } else if (command == exit) {
            notifyDestroyed();
        }
    }

    @Override
    protected void pauseApp() { }

    @Override
    protected void destroyApp(boolean unconditional) throws MIDletStateChangeException { }
}
