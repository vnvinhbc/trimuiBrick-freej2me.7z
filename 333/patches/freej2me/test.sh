#!/bin/sh
set -eu

PATCH_DIR=$(CDPATH= cd -- "$(dirname -- "$0")" && pwd)
REPO_DIR=$(CDPATH= cd -- "$PATCH_DIR/../.." && pwd)
ACTIVE_JAR="$REPO_DIR/Emus/JAVA/zulu17/bin/freej2me-sdl.jar"
BACKUP_JAR="$ACTIVE_JAR.pre-virtual-keyboard.bak"
BASE_JAR="$ACTIVE_JAR"
if [ -f "$BACKUP_JAR" ]; then
    BASE_JAR="$BACKUP_JAR"
fi

BUILD_DIR="$PATCH_DIR/build"
CLASS_DIR="$BUILD_DIR/classes"
TEST_CLASS_DIR="$BUILD_DIR/test-classes"
mkdir -p "$CLASS_DIR" "$TEST_CLASS_DIR"

SOURCE_LIST="$BUILD_DIR/sources.list"
TEST_LIST="$BUILD_DIR/tests.list"
find "$PATCH_DIR/src" -name '*.java' -type f 2>/dev/null | sort > "$SOURCE_LIST" || true
if [ -s "$SOURCE_LIST" ]; then
    javac --release 17 -cp "$BASE_JAR" -d "$CLASS_DIR" @"$SOURCE_LIST"
fi

find "$PATCH_DIR/test" -name '*.java' -type f | sort > "$TEST_LIST"
javac --release 17 -cp "$CLASS_DIR:$BASE_JAR" -d "$TEST_CLASS_DIR" @"$TEST_LIST"

if [ "${1:-}" = "--installed" ]; then
    shift
    BASE_JAR="$ACTIVE_JAR"
    RUNTIME_CP="$TEST_CLASS_DIR:$ACTIVE_JAR"
else
    RUNTIME_CP="$TEST_CLASS_DIR:$CLASS_DIR:$BASE_JAR"
fi

if [ "$#" -gt 0 ]; then
    TESTS="$*"
else
    TESTS="VirtualKeyboardModelTest VirtualKeyboardViewTest FormVirtualKeyboardTest TextBoxVirtualKeyboardTest PackagingTest"
fi

for test_name in $TESTS; do
    (cd "$(dirname "$BASE_JAR")" && \
        java -ea -Djava.awt.headless=true -Dfreej2me.patch.dir="$PATCH_DIR" \
        -cp "$RUNTIME_CP" \
        "javax.microedition.lcdui.$test_name")
done
