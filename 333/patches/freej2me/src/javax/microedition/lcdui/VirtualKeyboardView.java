package javax.microedition.lcdui;

import java.awt.Rectangle;
import java.util.List;

import org.recompile.mobile.PlatformGraphics;

final class VirtualKeyboardView {
    private static final int BACKGROUND = 0x171A1F;
    private static final int PREVIEW_BACKGROUND = 0x0D0F12;
    private static final int KEY_BACKGROUND = 0x3B414A;
    private static final int SELECTED_BACKGROUND = 0xE9B949;
    private static final int TEXT = 0xFFFFFF;
    private static final int SELECTED_TEXT = 0x111111;
    private static final int BORDER = 0x737B87;

    private VirtualKeyboardView() { }

    static void render(PlatformGraphics graphics, int width, int height,
                       VirtualKeyboardModel model, String title) {
        graphics.setColor(BACKGROUND);
        graphics.fillRect(0, 0, width, height);

        int previewHeight = previewHeight(height);
        graphics.setColor(PREVIEW_BACKGROUND);
        graphics.fillRect(0, 0, width, previewHeight);
        graphics.setColor(TEXT);
        String prefix = title == null || title.isEmpty() ? "" : title + ": ";
        String preview = fitFromEnd(prefix, model.getDisplayValue() + "|", width);
        graphics.drawString(preview, 4, 4, Graphics.TOP | Graphics.LEFT);

        for (int row = 0; row < model.getRows().size(); row++) {
            List<VirtualKeyboardModel.Key> keys = model.getRows().get(row);
            for (int column = 0; column < keys.size(); column++) {
                Rectangle bounds = keyBounds(width, height, model, row, column);
                boolean selected = row == model.getSelectedRow()
                        && column == model.getSelectedColumn();
                graphics.setColor(selected ? SELECTED_BACKGROUND : KEY_BACKGROUND);
                graphics.fillRect(bounds.x + 1, bounds.y + 1,
                        Math.max(1, bounds.width - 2), Math.max(1, bounds.height - 2));
                graphics.setColor(BORDER);
                graphics.drawRect(bounds.x, bounds.y,
                        Math.max(0, bounds.width - 1), Math.max(0, bounds.height - 1));
                graphics.setColor(selected ? SELECTED_TEXT : TEXT);
                graphics.drawString(displayLabel(keys.get(column), width),
                        bounds.x + bounds.width / 2,
                        bounds.y + bounds.height / 2,
                        Graphics.HCENTER | Graphics.VCENTER);
            }
        }
    }

    static Rectangle keyBounds(int width, int height, VirtualKeyboardModel model,
                               int row, int column) {
        int rowCount = model.getRows().size();
        int columnCount = model.getRows().get(row).size();
        int top = previewHeight(height);
        int gridHeight = Math.max(1, height - top);
        int x1 = column * width / columnCount;
        int x2 = (column + 1) * width / columnCount;
        int y1 = top + row * gridHeight / rowCount;
        int y2 = top + (row + 1) * gridHeight / rowCount;
        return new Rectangle(x1, y1, Math.max(1, x2 - x1), Math.max(1, y2 - y1));
    }

    private static int previewHeight(int height) {
        return Math.min(height - 1, Math.max(22, Math.min(42, height / 4)));
    }

    private static String displayLabel(VirtualKeyboardModel.Key key, int width) {
        if (width > 176) return key.getLabel();
        switch (key.getAction()) {
            case SPACE: return "SP";
            case BACKSPACE: return "<-";
            case SHIFT: return "SH";
            case DONE: return "OK";
            case CANCEL: return "X";
            default: return key.getLabel();
        }
    }

    private static String fitFromEnd(String prefix, String value, int width) {
        int maxCharacters = Math.max(4, (width - 8) / 7);
        String full = prefix + value;
        if (full.length() <= maxCharacters) return full;
        int keep = Math.max(1, maxCharacters - 3);
        return "..." + full.substring(full.length() - keep);
    }
}
