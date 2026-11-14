# Virtual keyboard for FreeJ2ME

## Intent

Allow TrimUI Brick users to enter text in Java ME games without a physical keyboard, using only the D-pad and OK button.

## Goals

- Recognize the standard Java ME `TextBox` screen and a selected editable `TextField` inside a `Form`.
- Open an on-screen keyboard automatically when the user presses OK on an editable text field.
- Navigate keys with Up, Down, Left, and Right; activate the selected key with OK.
- Provide lowercase and uppercase Latin letters, digits, common symbols, Space, Backspace, Shift, Done, and Cancel.
- Respect `maxSize`, `UNEDITABLE`, `NUMERIC`, `DECIMAL`, `PHONENUMBER`, `EMAILADDR`, `URL`, and `PASSWORD` constraints where practical.
- Return the edited value through the existing `TextField`/`TextBox` API and notify the `Form` item-state listener after a committed `TextField` edit.

## Non-goals

- Reliably detecting text fields drawn manually by a game on a low-level `Canvas`.
- Vietnamese input methods, predictive text, clipboard integration, or touch input.
- Changing controls outside the keyboard overlay.

## Interaction

- `TextBox`: the keyboard appears when the screen becomes current.
- `TextField`: Up/Down selects the field as today; OK opens the keyboard.
- While open, game key events are consumed by the keyboard.
- The top area shows the current value and caret; password values are masked.
- `Done` commits the value and closes the overlay. `Cancel` restores the original value.
- Rows wrap horizontally and vertically so every key is reachable with the D-pad.

## Implementation

- Vendor the matching `freej2mesa` source used by the packaged JAR and keep the existing SDL/native runtime binaries.
- Add a reusable virtual-keyboard state/model to the Java ME LCDUI layer.
- Integrate it with `TextBox`, `Form`, and rendering/input dispatch rather than recognizing pixels from game frames.
- Rebuild `freej2me-sdl.jar` and replace the packaged JAR only after tests pass; preserve a checksum-recorded backup during development.
- Add unit tests for navigation, editing, constraints, maximum length, commit, and cancel, plus a small MIDlet smoke test for `TextBox` and `TextField`.

## Approaches considered

- **Selected: rebuild from matching source.** Maintainable, testable, and suitable for future fixes.
- Patch compiled bytecode in place. Rejected because it is fragile and difficult to test or maintain.

## Constraints and risks

- The current repository is a runtime bundle and contains no emulator source or functional Git metadata.
- Exact source compatibility must be checked against the current JAR before replacing it.
- Hardware execution cannot be fully verified in this environment; desktop/model tests and JAR-level verification will be used, with a final device test checklist supplied.
