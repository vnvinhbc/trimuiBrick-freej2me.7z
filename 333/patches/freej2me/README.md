# FreeJ2ME virtual keyboard patch

This source overlay adds a virtual keyboard for standard Java ME `TextField` and `TextBox` controls in the bundled FreeJ2ME runtime.

## Controls

- D-pad: move between keys.
- OK: activate the selected key.
- `Space`: insert a space.
- `Backspace`: remove the last character.
- `Shift`: switch Latin letter case.
- `Done`: commit text and close the keyboard.
- `Cancel`: discard the edit and close the keyboard.

The keyboard supports normal text, numeric, decimal, phone, email, URL, password, maximum-length, and uneditable constraints. It is laid out for 128x128, 176x208, 240x320, 320x240, and 640x360. Games that draw their own input UI on a low-level `Canvas` cannot be detected reliably.

## Rebuild and test

```sh
patches/freej2me/test.sh
patches/freej2me/test-reproducible-build.sh
patches/freej2me/build.sh
```

The build creates `build/freej2me-sdl.jar` and `build/virtual-keyboard-smoke.jar` without changing the installed runtime.

## Install and restore

```sh
patches/freej2me/install.sh
```

The installer verifies the known original checksum and saves it once as `Emus/JAVA/zulu17/bin/freej2me-sdl.jar.pre-virtual-keyboard.bak` before an atomic replacement.

To restore the original:

```sh
cp Emus/JAVA/zulu17/bin/freej2me-sdl.jar.pre-virtual-keyboard.bak Emus/JAVA/zulu17/bin/freej2me-sdl.jar
```

## TrimUI acceptance check

1. Copy `build/virtual-keyboard-smoke.jar` to a `Roms/JAVA/240320` folder and launch it.
2. Select each Form field, press OK, enter text, use Backspace and Shift, then test Cancel and Done.
3. Confirm the locked field does not open the keyboard and the password preview is masked.
4. Open the TextBox using its command and confirm the keyboard opens automatically.
5. Repeat with one normal 240x320 game and one 320x240 game.
6. After closing the keyboard, confirm normal gameplay controls still work.

The AArch64 Java/SDL runtime cannot be executed on the x86 development host, so these final device checks remain required.
