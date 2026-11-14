package javax.microedition.lcdui;

import org.recompile.mobile.Mobile;
import org.recompile.mobile.PlatformGraphics;
import org.recompile.mobile.PlatformImage;

public class TextBox extends Screen {
    private String title;
    private String text;
    private int max;
    private int constraints;
    private String mode;
    private Ticker ticker;

    private VirtualKeyboardModel keyboard;
    private int keyboardProfile;

    public TextBox(String title, String value, int maxSize, int constraints) {
        this.title = title;
        text = value;
        max = maxSize;
        this.constraints = constraints;
        platformImage = new PlatformImage(width, height);
        render();
    }

    public void delete(int offset, int length) {
        text = text.substring(0, offset) + text.substring(offset + length);
    }

    public int getCaretPosition() { return 0; }

    public int getChars(char[] data) {
        for (int i = 0; i < text.length(); i++) data[i] = text.charAt(i);
        return text.length();
    }

    public int getConstraints() { return constraints; }
    public int getMaxSize() { return max; }
    public String getString() { return text; }

    public void insert(char[] data, int offset, int length, int position) {
        StringBuilder out = new StringBuilder();
        out.append(text, 0, position);
        out.append(data, offset, length);
        out.append(text.substring(position));
        text = out.toString();
    }

    public void insert(String source, int position) {
        StringBuilder out = new StringBuilder();
        out.append(text, 0, position);
        out.append(source);
        out.append(text.substring(position));
        text = out.toString();
    }

    public void setChars(char[] data, int offset, int length) {
        StringBuilder out = new StringBuilder();
        out.append(data, offset, length);
        text = out.toString();
    }

    public void setConstraints(int constraints) { this.constraints = constraints; }
    public void setInitialInputMode(String characterSubset) { mode = characterSubset; }
    public int setMaxSize(int maxSize) { max = maxSize; return max; }
    public void setString(String value) { text = value; }
    public void setTicker(Ticker ticker) { this.ticker = ticker; }
    public void setTitle(String title) { this.title = title; }
    public int size() { return text.length(); }

    @Override
    public void keyPressed(int keyCode) {
        if (keyboard != null) {
            handleKeyboardKey(keyCode);
            return;
        }
        if (listCommands) {
            keyPressedCommands(keyCode);
            return;
        }
        if (keyCode == Mobile.NOKIA_SOFT1) {
            doLeftCommand();
        } else if (keyCode == Mobile.NOKIA_SOFT2) {
            doRightCommand();
        } else if (isFire(keyCode)) {
            doDefaultCommand();
        }
        render();
    }

    @Override
    public void notifySetCurrent() {
        keyboardProfile = 0;
        if ((constraints & TextField.UNEDITABLE) == 0) {
            keyboard = new VirtualKeyboardModel(text, max, constraints);
        } else {
            keyboard = null;
        }
        render();
    }

    @Override
    protected void render() {
        if (platformImage == null) return;
        if (listCommands && keyboard == null) {
            renderCommands();
            return;
        }
        if (keyboard != null) {
            VirtualKeyboardView.render(platformImage.getGraphics(), width, height, keyboard, title);
        } else {
            renderClosed(platformImage.getGraphics());
        }
        repaintIfCurrent();
    }

    boolean isVirtualKeyboardOpen() { return keyboard != null; }

    private void handleKeyboardKey(int keyCode) {
        VirtualKeyboardModel.Direction direction = directionFor(keyCode);
        if (direction != null) {
            keyboard.move(direction);
            render();
            return;
        }
        if (!isFire(keyCode)) return;
        if (keyboardProfile == 0) keyboardProfile = profileForFire(keyCode);

        VirtualKeyboardModel.Result result = keyboard.activate();
        if (result == VirtualKeyboardModel.Result.COMMIT) {
            text = keyboard.getDraft();
            keyboard = null;
        } else if (result == VirtualKeyboardModel.Result.CANCEL) {
            keyboard = null;
        }
        render();
    }

    private void renderClosed(PlatformGraphics graphics) {
        graphics.setColor(0xFFFFFF);
        graphics.fillRect(0, 0, width, height);
        graphics.setColor(0x000000);
        graphics.drawString(title == null ? "" : title, width / 2, 2,
                Graphics.HCENTER | Graphics.TOP);
        graphics.drawLine(0, 20, width, 20);
        graphics.drawLine(0, height - 20, width, height - 20);
        graphics.drawString(displayText(), 4, 28, Graphics.LEFT | Graphics.TOP);
        renderCommandLabels(graphics);
    }

    private String displayText() {
        String value = text == null ? "" : text;
        if ((constraints & TextField.PASSWORD) == 0) return value;
        StringBuilder masked = new StringBuilder(value.length());
        for (int i = 0; i < value.length(); i++) masked.append('*');
        return masked.toString();
    }

    private void renderCommandLabels(PlatformGraphics graphics) {
        if (commands.isEmpty()) return;
        if (commands.size() > 2) {
            graphics.drawString("Options", 3, height - 17, Graphics.LEFT | Graphics.TOP);
            return;
        }
        graphics.drawString(commands.get(0).getLabel(), 3, height - 17,
                Graphics.LEFT | Graphics.TOP);
        if (commands.size() == 2) {
            graphics.drawString(commands.get(1).getLabel(), width - 3, height - 17,
                    Graphics.RIGHT | Graphics.TOP);
        }
    }

    private void repaintIfCurrent() {
        Display display = getDisplay();
        if (display != null && display.getCurrent() == this) {
            Mobile.getPlatform().repaint(platformImage, 0, 0, width, height);
        }
    }

    private static boolean isFire(int keyCode) {
        return keyCode == Mobile.KEY_NUM5 || keyCode == Mobile.NOKIA_SOFT3
                || keyCode == Mobile.SIEMENS_FIRE || keyCode == Mobile.MOTOROLA_FIRE;
    }

    private static int profileForFire(int keyCode) {
        if (keyCode == Mobile.SIEMENS_FIRE) return 2;
        if (keyCode == Mobile.MOTOROLA_FIRE) return 3;
        if (keyCode == Mobile.NOKIA_SOFT3) return 1;
        return 0;
    }

    private VirtualKeyboardModel.Direction directionFor(int keyCode) {
        if (keyCode == Mobile.KEY_NUM2) return VirtualKeyboardModel.Direction.UP;
        if (keyCode == Mobile.KEY_NUM8) return VirtualKeyboardModel.Direction.DOWN;
        if (keyCode == Mobile.KEY_NUM4) return VirtualKeyboardModel.Direction.LEFT;
        if (keyCode == Mobile.KEY_NUM6) return VirtualKeyboardModel.Direction.RIGHT;
        if (keyboardProfile == 2) {
            if (keyCode == Mobile.SIEMENS_UP) return VirtualKeyboardModel.Direction.UP;
            if (keyCode == Mobile.SIEMENS_DOWN) return VirtualKeyboardModel.Direction.DOWN;
            if (keyCode == Mobile.SIEMENS_LEFT) return VirtualKeyboardModel.Direction.LEFT;
            if (keyCode == Mobile.SIEMENS_RIGHT) return VirtualKeyboardModel.Direction.RIGHT;
        } else if (keyboardProfile == 3) {
            if (keyCode == Mobile.MOTOROLA_UP) return VirtualKeyboardModel.Direction.UP;
            if (keyCode == Mobile.MOTOROLA_DOWN) return VirtualKeyboardModel.Direction.DOWN;
            if (keyCode == Mobile.MOTOROLA_LEFT) return VirtualKeyboardModel.Direction.LEFT;
            if (keyCode == Mobile.MOTOROLA_RIGHT) return VirtualKeyboardModel.Direction.RIGHT;
        } else {
            if (keyCode == Mobile.NOKIA_UP) return VirtualKeyboardModel.Direction.UP;
            if (keyCode == Mobile.NOKIA_DOWN) return VirtualKeyboardModel.Direction.DOWN;
            if (keyCode == Mobile.NOKIA_LEFT) return VirtualKeyboardModel.Direction.LEFT;
            if (keyCode == Mobile.NOKIA_RIGHT) return VirtualKeyboardModel.Direction.RIGHT;
        }
        return null;
    }
}
