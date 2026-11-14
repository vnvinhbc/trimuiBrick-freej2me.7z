#!/bin/sh
set -eu

PATCH_DIR=$(CDPATH= cd -- "$(dirname -- "$0")" && pwd)
REPO_DIR=$(CDPATH= cd -- "$PATCH_DIR/../.." && pwd)
ACTIVE_JAR="$REPO_DIR/Emus/JAVA/zulu17/bin/freej2me-sdl.jar"
BASE_JAR="$ACTIVE_JAR"
if [ -f "$ACTIVE_JAR.pre-virtual-keyboard.bak" ]; then
    BASE_JAR="$ACTIVE_JAR.pre-virtual-keyboard.bak"
fi
BUILD_JAR="$PATCH_DIR/build/freej2me-sdl.jar"
TEST_DIR=$(mktemp -d /tmp/freej2me-install-test.XXXXXX)
trap 'rm -rf "$TEST_DIR"' EXIT HUP INT TERM

cp "$BASE_JAR" "$TEST_DIR/freej2me-sdl.jar"
FREEJ2ME_ACTIVE_JAR="$TEST_DIR/freej2me-sdl.jar" \
FREEJ2ME_BUILD_JAR="$BUILD_JAR" \
    "$PATCH_DIR/install.sh"

cmp "$BASE_JAR" "$TEST_DIR/freej2me-sdl.jar.pre-virtual-keyboard.bak"
cmp "$BUILD_JAR" "$TEST_DIR/freej2me-sdl.jar"

FREEJ2ME_ACTIVE_JAR="$TEST_DIR/freej2me-sdl.jar" \
FREEJ2ME_BUILD_JAR="$BUILD_JAR" \
    "$PATCH_DIR/install.sh"
cmp "$BASE_JAR" "$TEST_DIR/freej2me-sdl.jar.pre-virtual-keyboard.bak"

printf 'not a jar\n' > "$TEST_DIR/unknown.jar"
if FREEJ2ME_ACTIVE_JAR="$TEST_DIR/unknown.jar" \
   FREEJ2ME_BUILD_JAR="$BUILD_JAR" \
       "$PATCH_DIR/install.sh" >/dev/null 2>&1; then
    echo "installer accepted an unknown base" >&2
    exit 1
fi

echo "test-install: PASS"
