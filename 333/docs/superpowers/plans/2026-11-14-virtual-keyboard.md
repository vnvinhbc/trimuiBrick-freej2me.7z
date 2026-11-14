# FreeJ2ME Virtual Keyboard Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Add automatic standard Java ME text-input detection and a D-pad/OK-operated virtual keyboard to the packaged FreeJ2ME runtime.

**Architecture:** Keep the packaged emulator as the compatibility baseline and maintain a source-level overlay for the LCDUI classes that need text editing. A pure keyboard model owns layout, navigation, constraints, draft text, commit, and cancel; `Form` and `TextBox` adapt that model to FreeJ2ME rendering and input. A deterministic build script compiles with Java 17 compatibility and updates a copy of the JAR only after tests pass.

**Tech Stack:** Java 17 bytecode, FreeJ2ME LCDUI/PlatformGraphics, shell build scripts, JDK `javac`/`jar`, assertion-based Java tests.

**Spec:** `docs/superpowers/specs/2026-11-14-virtual-keyboard-design.md`

## Global Constraints

- Recognize only standard Java ME `TextBox` and selected `TextField`; custom Canvas fields are out of scope.
- All keyboard interaction uses Up, Down, Left, Right, and OK.
- Provide Latin letters, digits, common symbols, Space, Backspace, Shift, Done, and Cancel.
- Respect `maxSize`, `UNEDITABLE`, `NUMERIC`, `DECIMAL`, `PHONENUMBER`, `EMAILADDR`, `URL`, and `PASSWORD` where practical.
- Preserve the current native SDL binary, Java runtime, manifest, resources, and all unrelated JAR classes.
- Compile with `javac --release 17`; introduce no external dependency.
- Build and test a copied JAR before replacing `Emus/JAVA/zulu17/bin/freej2me-sdl.jar`.
- The workspace has no functional Git metadata, so task checkpoints use tests and SHA-256 manifests instead of commits.

## Review Focus

- Empty and `null` initial values must open as an empty draft without throwing; test in Task 1.
- A final uneven keyboard row must navigate and wrap without selecting a nonexistent cell; test in Task 1.
- Repeated OK/key-repeat events must not insert duplicate characters unintentionally; test press-only behavior in Tasks 3 and 4.
- A game changing the field value or screen during editing must not corrupt another target; test target isolation in Tasks 3 and 4.
- The overlay must remain usable at all bundled resolutions, especially 128x128 and 320x240; render bounds tests in Task 2.

---

### Task 1: Reproducible Patch Build and Keyboard Model

**Files:**
- Create: `patches/freej2me/build.sh`
- Create: `patches/freej2me/test.sh`
- Create: `patches/freej2me/src/javax/microedition/lcdui/VirtualKeyboardModel.java`
- Test: `patches/freej2me/test/javax/microedition/lcdui/VirtualKeyboardModelTest.java`
- Create: `patches/freej2me/BASE_JAR.sha256`

**Interfaces:**
- Consumes: `TextField` constraint constants from the packaged baseline JAR.
- Produces: `VirtualKeyboardModel(String initialValue, int maxSize, int constraints)`; `void move(Direction direction)`; `Result activate()`; `String getDraft()`; `String getDisplayValue()`; `List<List<Key>> getRows()`; `int getSelectedRow()`; `int getSelectedColumn()`; `boolean isShifted()`; `boolean isEditable()`; enums `Direction { UP, DOWN, LEFT, RIGHT }`, `Result { CONTINUE, COMMIT, CANCEL }`, and immutable `Key` values exposing `String getLabel()` and `Action getAction()` where `Action { CHARACTER, SPACE, BACKSPACE, SHIFT, DONE, CANCEL }`.

- [ ] **Step 1: Write failing model tests**

Add named tests for: QWERTY selection/navigation and wrap; uneven-row clamping; append; Backspace; Space; Shift/case; Done; Cancel; `null`/empty initial value; max length; numeric-only; decimal with one decimal separator; phone characters; email characters; URL characters; password masking; and `UNEDITABLE` rejection.

- [ ] **Step 2: Run the model test to verify it fails**

Run: `patches/freej2me/test.sh VirtualKeyboardModelTest`

Expected: FAIL because `VirtualKeyboardModel` does not exist.

- [ ] **Step 3: Implement `VirtualKeyboardModel`**

Use fixed logical rows for normal text and smaller constraint-specific layouts. Treat `constraints & TextField.CONSTRAINT_MASK` as the base type and independent flags such as `PASSWORD`/`UNEDITABLE` as modifiers. Keep the caret at the end; Backspace removes the final UTF-16 code unit. `activate()` returns `COMMIT` or `CANCEL` only for those action keys.

- [ ] **Step 4: Add deterministic build/test scripts**

`build.sh` must verify `BASE_JAR.sha256`, compile patch sources with `javac --release 17 -cp <base-jar>`, copy the baseline to `patches/freej2me/build/freej2me-sdl.jar`, and update only compiled overlay classes. Before installation `<base-jar>` is the packaged JAR; afterward it is the checksum-matched `.pre-virtual-keyboard.bak`. `test.sh` must compile tests against overlay classes plus that baseline and run one named test main or the complete suite.

- [ ] **Step 5: Run model tests**

Run: `patches/freej2me/test.sh VirtualKeyboardModelTest`

Expected: all named model tests print `PASS`; process exits 0.

- [ ] **Step 6: Record checkpoint**

Run: `sha256sum Emus/JAVA/zulu17/bin/freej2me-sdl.jar patches/freej2me/build/freej2me-sdl.jar`

Expected: baseline checksum remains `9a947c5a7260654d36df08ff915421e22cd0241e3cff0e374a676c5ba42f52c5`; build artifact differs after overlay classes are added.

### Task 2: Resolution-Safe Keyboard Renderer

**Files:**
- Create: `patches/freej2me/src/javax/microedition/lcdui/VirtualKeyboardView.java`
- Test: `patches/freej2me/test/javax/microedition/lcdui/VirtualKeyboardViewTest.java`

**Interfaces:**
- Consumes: the getters produced by `VirtualKeyboardModel`.
- Produces: `static void render(PlatformGraphics graphics, int width, int height, VirtualKeyboardModel model, String title)` and package-private `static Rectangle keyBounds(int width, int height, VirtualKeyboardModel model, int row, int column)` for deterministic bounds tests.

- [ ] **Step 1: Write failing renderer tests**

Render normal and password models into `PlatformImage` instances sized 128x128, 176x208, 240x320, 320x240, and 640x360. Assert no exception, all calculated key rectangles stay within bounds, selected-key pixels differ from unselected-key pixels, and password preview does not expose plaintext.

- [ ] **Step 2: Run renderer tests to verify they fail**

Run: `patches/freej2me/test.sh VirtualKeyboardViewTest`

Expected: FAIL because `VirtualKeyboardView` does not exist.

- [ ] **Step 3: Implement `VirtualKeyboardView.render(...)`**

Draw an opaque full-screen overlay, a one-line title/value preview with an end caret, then equal-height keyboard rows. Derive cell bounds from runtime width and each row's key count; use abbreviated action labels `SP`, `<-`, `SH`, `OK`, and `X` at 128px width and full English labels when space permits.

- [ ] **Step 4: Run renderer tests**

Run: `patches/freej2me/test.sh VirtualKeyboardViewTest`

Expected: all five resolution cases and password case print `PASS`; process exits 0.

- [ ] **Step 5: Record checkpoint**

Run: `patches/freej2me/build.sh && jar tf patches/freej2me/build/freej2me-sdl.jar | rg 'VirtualKeyboard(Model|View)'`

Expected: both new classes and nested classes appear exactly once.

### Task 3: Editable `TextField` Integration in `Form`

**Files:**
- Create: `patches/freej2me/src/javax/microedition/lcdui/Form.java` from the exact public API and behavior of the packaged class, adding only virtual-keyboard integration.
- Test: `patches/freej2me/test/javax/microedition/lcdui/FormVirtualKeyboardTest.java`

**Interfaces:**
- Consumes: `VirtualKeyboardModel`, `VirtualKeyboardView`, existing `Form.items`, `Form.currentItem`, and `Form.listener`.
- Produces: unchanged public `Form` API; OK opens an editor for the selected editable `TextField`; keyboard input is consumed while editing; Done commits and Cancel restores.

- [ ] **Step 1: Write failing Form integration tests**

Set a `MobilePlatform`, construct Forms containing mixed `StringItem` and `TextField` items, and assert: OK edits only the selected `TextField`; navigation does not leak to Form selection while open; Done updates `getString()` and invokes `ItemStateListener` once; Cancel preserves the original; `UNEDITABLE` does not open; two fields remain isolated; repeated key events do not double-activate.

- [ ] **Step 2: Run Form tests to verify they fail**

Run: `patches/freej2me/test.sh FormVirtualKeyboardTest`

Expected: FAIL because packaged `Form` invokes its default command instead of opening the keyboard.

- [ ] **Step 3: Implement source-compatible Form integration**

Copy the baseline constructor/list mutation/command/selection behavior exactly. Add package-private `boolean isVirtualKeyboardOpen()` for tests, open on `Mobile.KEY_NUM5` and each phone-family fire code, route direction/OK presses to the model while open, call `listener.itemStateChanged(field)` once after Done, and render the view over `platformImage`. Draw each visible `TextField` value on the existing item row, masking password values.

- [ ] **Step 4: Run model, renderer, and Form tests**

Run: `patches/freej2me/test.sh`

Expected: Tasks 1-3 suites print `PASS`; process exits 0.

- [ ] **Step 5: Verify public API compatibility**

Run: `javap -classpath Emus/JAVA/zulu17/bin/freej2me-sdl.jar -public javax.microedition.lcdui.Form > /tmp/form-before.txt && javap -classpath patches/freej2me/build/freej2me-sdl.jar -public javax.microedition.lcdui.Form > /tmp/form-after.txt && diff -u /tmp/form-before.txt /tmp/form-after.txt`

Expected: only package-private test hook is absent from `-public`; no public API diff.

### Task 4: Automatic `TextBox` Integration

**Files:**
- Create: `patches/freej2me/src/javax/microedition/lcdui/TextBox.java` from the exact public API of the packaged class, adding rendering/input behavior.
- Test: `patches/freej2me/test/javax/microedition/lcdui/TextBoxVirtualKeyboardTest.java`

**Interfaces:**
- Consumes: `VirtualKeyboardModel`, `VirtualKeyboardView`, inherited command dispatch, and existing `TextBox` value/constraint API.
- Produces: unchanged public `TextBox` API; `notifySetCurrent()` opens the keyboard once per display activation; Done commits and returns to the non-overlay TextBox; Cancel restores.

- [ ] **Step 1: Write failing TextBox integration tests**

Assert automatic opening on `notifySetCurrent()`, initial-value editing, Done, Cancel, max length, password masking, command availability after Done, no immediate reopen on repaint, reopening after a later display activation, target isolation when the current Displayable changes, and press-only activation under key repeat.

- [ ] **Step 2: Run TextBox tests to verify they fail**

Run: `patches/freej2me/test.sh TextBoxVirtualKeyboardTest`

Expected: FAIL because packaged `TextBox` has no rendering or key handling.

- [ ] **Step 3: Implement source-compatible TextBox integration**

Initialize `platformImage`, render title/value/commands when closed, open a fresh model from the current value in `notifySetCurrent()`, and use the same direction/fire normalization as Form. Keep a per-activation dismissed flag so Done/Cancel does not immediately reopen. Preserve all existing string mutation methods and enforce their documented bounds/max-size behavior when touched by the keyboard.

- [ ] **Step 4: Run the full Java test suite**

Run: `patches/freej2me/test.sh`

Expected: all four suites print `PASS`; process exits 0.

- [ ] **Step 5: Verify public API compatibility**

Run the same `javap -public`/`diff` procedure for `TextBox`.

Expected: no public member is removed or has a changed signature.

### Task 5: Package, Smoke-Test, and Install Safely

**Files:**
- Create: `patches/freej2me/test/javax/microedition/lcdui/VirtualKeyboardSmokeMidlet.java`
- Create: `patches/freej2me/install.sh`
- Create: `patches/freej2me/README.md`
- Create: `patches/freej2me/ARTIFACTS.sha256`
- Modify: `Emus/JAVA/zulu17/bin/freej2me-sdl.jar`

**Interfaces:**
- Consumes: all overlay sources and tests from Tasks 1-4.
- Produces: a verified packaged JAR, reproducible rebuild instructions, a recoverable original backup, and device test steps.

- [ ] **Step 1: Add the smoke MIDlet and packaging checks**

The MIDlet switches between one `Form` containing editable, numeric, password, and uneditable fields and one `TextBox`. Add checks that the patched JAR retains `Main-Class: org.recompile.freej2me.Anbu`, opens with `jar tf`, contains all original entries other than replaced classes, and can load the smoke MIDlet without `VerifyError`/`NoSuchMethodError`.

- [ ] **Step 2: Run build and complete verification**

Run: `patches/freej2me/test.sh && patches/freej2me/build.sh`

Expected: tests pass; build exits 0; patched JAR is produced.

- [ ] **Step 3: Test packaged class loading with bundled Java 17**

Run: `/usr/bin/java -Xverify:all -cp patches/freej2me/build/test-classes:patches/freej2me/build/classes:Emus/JAVA/zulu17/bin/freej2me-sdl.jar javax.microedition.lcdui.VirtualKeyboardModelTest`

Expected: no verifier/linkage error and all tests pass with Java 17-compatible bytecode. The bundled `java` and `sdl_interface` are AArch64 binaries and therefore receive their runtime check on the TrimUI device, not on this x86 host.

- [ ] **Step 4: Implement and run safe installation**

`install.sh` must refuse an unknown baseline checksum, create `Emus/JAVA/zulu17/bin/freej2me-sdl.jar.pre-virtual-keyboard.bak` once, install via a temporary sibling plus atomic rename, and verify the installed checksum.

Run: `patches/freej2me/install.sh`

Expected: original backup exists, installed JAR equals the tested build artifact, and the native `sdl_interface` checksum is unchanged.

- [ ] **Step 5: Re-run tests against the installed JAR**

Run: `patches/freej2me/test.sh --installed && unzip -t Emus/JAVA/zulu17/bin/freej2me-sdl.jar`

Expected: every suite passes and unzip reports no errors.

- [ ] **Step 6: Document device acceptance checks and hashes**

In `README.md`, record controls, supported constraints/resolutions, restore command, limitations, and TrimUI checks: launch one 240x320 and one 320x240 game; open a standard field; enter mixed text; Backspace; Shift; Cancel; Done; verify gameplay controls after close. Write checksums for the baseline backup, installed JAR, and `sdl_interface` to `ARTIFACTS.sha256`.
