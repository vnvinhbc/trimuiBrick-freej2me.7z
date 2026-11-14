package javax.microedition.lcdui;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

final class VirtualKeyboardModel {
    enum Direction { UP, DOWN, LEFT, RIGHT }
    enum Result { CONTINUE, COMMIT, CANCEL }
    enum Action { CHARACTER, SPACE, BACKSPACE, SHIFT, DONE, CANCEL }

    static final class Key {
        private final String label;
        private final String value;
        private final Action action;

        private Key(String label, String value, Action action) {
            this.label = label;
            this.value = value;
            this.action = action;
        }

        String getLabel() { return label; }
        Action getAction() { return action; }
    }

    private final String original;
    private final int maxSize;
    private final int constraints;
    private final boolean editable;
    private String draft;
    private boolean shifted;
    private List<List<Key>> rows;
    private int selectedRow;
    private int selectedColumn;

    VirtualKeyboardModel(String initialValue, int maxSize, int constraints) {
        original = initialValue == null ? "" : initialValue;
        draft = original;
        this.maxSize = Math.max(0, maxSize);
        this.constraints = constraints;
        editable = (constraints & TextField.UNEDITABLE) == 0;
        rows = createRows();
    }

    void move(Direction direction) {
        if (direction == null || rows.isEmpty()) return;
        switch (direction) {
            case LEFT:
                selectedColumn = (selectedColumn + currentRow().size() - 1) % currentRow().size();
                break;
            case RIGHT:
                selectedColumn = (selectedColumn + 1) % currentRow().size();
                break;
            case UP:
                selectedRow = (selectedRow + rows.size() - 1) % rows.size();
                clampColumn();
                break;
            case DOWN:
                selectedRow = (selectedRow + 1) % rows.size();
                clampColumn();
                break;
            default:
                break;
        }
    }

    Result activate() {
        if (!editable) return Result.CONTINUE;
        Key key = currentRow().get(selectedColumn);
        switch (key.action) {
            case CHARACTER:
                append(key.value);
                return Result.CONTINUE;
            case SPACE:
                append(" ");
                return Result.CONTINUE;
            case BACKSPACE:
                if (!draft.isEmpty()) draft = draft.substring(0, draft.length() - 1);
                return Result.CONTINUE;
            case SHIFT:
                shifted = !shifted;
                rows = createRows();
                clampColumn();
                return Result.CONTINUE;
            case DONE:
                return Result.COMMIT;
            case CANCEL:
                draft = original;
                return Result.CANCEL;
            default:
                return Result.CONTINUE;
        }
    }

    String getDraft() { return draft; }

    String getDisplayValue() {
        if ((constraints & TextField.PASSWORD) == 0) return draft;
        StringBuilder masked = new StringBuilder(draft.length());
        for (int i = 0; i < draft.length(); i++) masked.append('*');
        return masked.toString();
    }

    List<List<Key>> getRows() { return rows; }
    int getSelectedRow() { return selectedRow; }
    int getSelectedColumn() { return selectedColumn; }
    boolean isShifted() { return shifted; }
    boolean isEditable() { return editable; }

    private List<Key> currentRow() { return rows.get(selectedRow); }

    private void clampColumn() {
        selectedColumn = Math.min(selectedColumn, currentRow().size() - 1);
    }

    private void append(String value) {
        if (value == null || value.isEmpty() || draft.length() >= maxSize) return;
        int baseConstraint = constraints & TextField.CONSTRAINT_MASK;
        if (baseConstraint == TextField.DECIMAL) {
            if (".".equals(value) && draft.indexOf('.') >= 0) return;
            if ("-".equals(value) && !draft.isEmpty()) return;
        } else if (baseConstraint == TextField.NUMERIC) {
            if ("-".equals(value) && !draft.isEmpty()) return;
        }
        if (draft.length() + value.length() <= maxSize) draft += value;
    }

    private List<List<Key>> createRows() {
        int baseConstraint = constraints & TextField.CONSTRAINT_MASK;
        List<List<Key>> result = new ArrayList<>();
        if (baseConstraint == TextField.NUMERIC) {
            result.add(characters("1234567890"));
            result.add(row(character("-"), action("Backspace", Action.BACKSPACE),
                    action("Done", Action.DONE), action("Cancel", Action.CANCEL)));
        } else if (baseConstraint == TextField.DECIMAL) {
            result.add(characters("1234567890"));
            result.add(row(character("."), character("-"), action("Backspace", Action.BACKSPACE),
                    action("Done", Action.DONE), action("Cancel", Action.CANCEL)));
        } else if (baseConstraint == TextField.PHONENUMBER) {
            result.add(characters("1234567890"));
            result.add(characters("+*# -()"));
            result.add(row(action("Backspace", Action.BACKSPACE), action("Done", Action.DONE),
                    action("Cancel", Action.CANCEL)));
        } else {
            String row0 = shifted ? "QWERTYUIOP" : "qwertyuiop";
            String row1 = shifted ? "ASDFGHJKL" : "asdfghjkl";
            String row2 = shifted ? "ZXCVBNM" : "zxcvbnm";
            result.add(characters(row0));
            result.add(characters(row1));
            result.add(characters(row2));
            result.add(characters("1234567890"));
            if (baseConstraint == TextField.EMAILADDR) {
                result.add(characters(".@_-+"));
            } else if (baseConstraint == TextField.URL) {
                result.add(characters(":/.?&=%#_-"));
            } else {
                result.add(characters(".,!?@-_/:;"));
            }
            result.add(row(action("Space", Action.SPACE), action("Backspace", Action.BACKSPACE),
                    action("Shift", Action.SHIFT), action("Done", Action.DONE),
                    action("Cancel", Action.CANCEL)));
        }

        List<List<Key>> immutable = new ArrayList<>(result.size());
        for (List<Key> row : result) immutable.add(Collections.unmodifiableList(row));
        return Collections.unmodifiableList(immutable);
    }

    private static List<Key> characters(String values) {
        List<Key> keys = new ArrayList<>(values.length());
        for (int i = 0; i < values.length(); i++) {
            String value = String.valueOf(values.charAt(i));
            keys.add(character(value));
        }
        return keys;
    }

    private static Key character(String value) {
        return new Key(value, value, Action.CHARACTER);
    }

    private static Key action(String label, Action action) {
        return new Key(label, null, action);
    }

    private static List<Key> row(Key... keys) {
        List<Key> row = new ArrayList<>(keys.length);
        Collections.addAll(row, keys);
        return row;
    }
}
