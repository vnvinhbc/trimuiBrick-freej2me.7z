package javax.microedition.lcdui;

import org.recompile.mobile.Mobile;
import org.recompile.mobile.PlatformGraphics;
import org.recompile.mobile.PlatformImage;

public class Form extends Screen {
    public ItemStateListener listener;

    private VirtualKeyboardModel keyboard;
    private TextField editingField;
    private int keyboardProfile;

    public Form(String title) {
        setTitle(title);
        platformImage = new PlatformImage(width, height);
        render();
    }

    public Form(String title, Item[] initialItems) {
        setTitle(title);
        if (items != null) {
            for (Item item : initialItems) items.add(item);
        }
        platformImage = new PlatformImage(width, height);
        render();
    }

    public int append(Image image) {
        items.add(new ImageItem("", image, 0, ""));
        render();
        return items.size() - 1;
    }

    public int append(Item item) {
        items.add(item);
        render();
        return items.size() - 1;
    }

    public int append(String value) {
        items.add(new StringItem("", value));
        render();
        return items.size() - 1;
    }

    public void delete(int itemNumber) {
        items.remove(itemNumber);
        render();
    }

    public void deleteAll() {
        items.clear();
        render();
    }

    public Item get(int itemNumber) { return items.get(itemNumber); }
    public int getHeight() { return 128; }
    public int getWidth() { return 64; }

    public void insert(int itemNumber, Item item) {
        items.add(itemNumber, item);
        render();
    }

    public void set(int itemNumber, Item item) {
        items.set(itemNumber, item);
        render();
    }

    public void setItemStateListener(ItemStateListener itemStateListener) {
        listener = itemStateListener;
    }

    public int size() { return items.size(); }

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
        if (items.isEmpty()) return;

        if (isFire(keyCode) && selectedItem() instanceof TextField) {
            TextField field = (TextField) selectedItem();
            if ((field.getConstraints() & TextField.UNEDITABLE) == 0) {
                keyboardProfile = profileForFire(keyCode);
                editingField = field;
                keyboard = new VirtualKeyboardModel(field.getString(), field.getMaxSize(),
                        field.getConstraints());
                render();
                return;
            }
        }

        switch (keyCode) {
            case Mobile.KEY_NUM2:
            case Mobile.NOKIA_UP:
                currentItem--;
                break;
            case Mobile.KEY_NUM8:
            case Mobile.NOKIA_DOWN:
                currentItem++;
                break;
            case Mobile.NOKIA_SOFT1:
                doLeftCommand();
                break;
            case Mobile.NOKIA_SOFT2:
                doRightCommand();
                break;
            case Mobile.NOKIA_SOFT3:
            case Mobile.KEY_NUM5:
                doDefaultCommand();
                break;
            default:
                break;
        }
        if (currentItem >= items.size()) currentItem = 0;
        if (currentItem < 0) currentItem = items.size() - 1;
        render();
    }

    @Override
    public void notifySetCurrent() { render(); }

    @Override
    protected void render() {
        super.render();
        if (platformImage == null) return;
        if (keyboard != null) {
            VirtualKeyboardView.render(platformImage.getGraphics(), width, height, keyboard,
                    editingField == null ? "Text" : editingField.getLabel());
        } else {
            renderTextFieldValues(platformImage.getGraphics());
        }
        repaintIfCurrent();
    }

    boolean isVirtualKeyboardOpen() { return keyboard != null; }

    private Item selectedItem() {
        if (currentItem < 0 || currentItem >= items.size()) return null;
        return items.get(currentItem);
    }

    private void handleKeyboardKey(int keyCode) {
        VirtualKeyboardModel.Direction direction = directionFor(keyCode);
        if (direction != null) {
            keyboard.move(direction);
            render();
            return;
        }
        if (!isFireForProfile(keyCode)) return;

        VirtualKeyboardModel.Result result = keyboard.activate();
        if (result == VirtualKeyboardModel.Result.COMMIT) {
            TextField committed = editingField;
            committed.setString(keyboard.getDraft());
            keyboard = null;
            editingField = null;
            if (listener != null) listener.itemStateChanged(committed);
        } else if (result == VirtualKeyboardModel.Result.CANCEL) {
            keyboard = null;
            editingField = null;
        }
        render();
    }

    private void renderTextFieldValues(PlatformGraphics graphics) {
        if (items.isEmpty() || currentItem < 0) return;
        int rowsPerPage = Math.max(1, (height - 50) / 15);
        rowsPerPage = Math.min(rowsPerPage, items.size());
        int start = (currentItem / rowsPerPage) * rowsPerPage;
        int end = Math.min(items.size(), start + rowsPerPage);
        int y = 25;
        for (int index = start; index < end; index++, y += 15) {
            Item item = items.get(index);
            if (!(item instanceof TextField)) continue;
            TextField field = (TextField) item;
            String value = maskedValue(field);
            graphics.setColor(index == currentItem ? 0xFFFFFF : 0x000000);
            graphics.drawString("[" + value + "]", width - 3, y, Graphics.RIGHT | Graphics.TOP);
        }
    }

    private static String maskedValue(TextField field) {
        String value = field.getString();
        if (value == null) value = "";
        if ((field.getConstraints() & TextField.PASSWORD) == 0) return value;
        StringBuilder masked = new StringBuilder(value.length());
        for (int i = 0; i < value.length(); i++) masked.append('*');
        return masked.toString();
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

    private boolean isFireForProfile(int keyCode) {
        switch (keyboardProfile) {
            case 1: return keyCode == Mobile.NOKIA_SOFT3;
            case 2: return keyCode == Mobile.SIEMENS_FIRE;
            case 3: return keyCode == Mobile.MOTOROLA_FIRE;
            default: return keyCode == Mobile.KEY_NUM5;
        }
    }

    private VirtualKeyboardModel.Direction directionFor(int keyCode) {
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
        } else if (keyboardProfile == 1) {
            if (keyCode == Mobile.NOKIA_UP) return VirtualKeyboardModel.Direction.UP;
            if (keyCode == Mobile.NOKIA_DOWN) return VirtualKeyboardModel.Direction.DOWN;
            if (keyCode == Mobile.NOKIA_LEFT) return VirtualKeyboardModel.Direction.LEFT;
            if (keyCode == Mobile.NOKIA_RIGHT) return VirtualKeyboardModel.Direction.RIGHT;
        }
        if (keyCode == Mobile.KEY_NUM2) return VirtualKeyboardModel.Direction.UP;
        if (keyCode == Mobile.KEY_NUM8) return VirtualKeyboardModel.Direction.DOWN;
        if (keyCode == Mobile.KEY_NUM4) return VirtualKeyboardModel.Direction.LEFT;
        if (keyCode == Mobile.KEY_NUM6) return VirtualKeyboardModel.Direction.RIGHT;
        return null;
    }
}
