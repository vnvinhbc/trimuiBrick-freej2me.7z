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

EXPECTED_HASH=$(awk 'NR == 1 { print $1 }' "$PATCH_DIR/BASE_JAR.sha256")
ACTUAL_HASH=$(sha256sum "$BASE_JAR" | awk '{ print $1 }')
if [ "$ACTUAL_HASH" != "$EXPECTED_HASH" ]; then
    echo "Refusing unknown base JAR: $ACTUAL_HASH" >&2
    exit 1
fi

BUILD_DIR="$PATCH_DIR/build"
CLASS_DIR="$BUILD_DIR/classes"
OUTPUT_JAR="$BUILD_DIR/freej2me-sdl.jar"
SMOKE_CLASS_DIR="$BUILD_DIR/smoke-classes"
SMOKE_JAR="$BUILD_DIR/virtual-keyboard-smoke.jar"
mkdir -p "$CLASS_DIR"
find "$CLASS_DIR" -mindepth 1 -delete
find "$PATCH_DIR/src" -name '*.java' -type f | sort > "$BUILD_DIR/sources.list"
javac --release 17 -cp "$BASE_JAR" -d "$CLASS_DIR" @"$BUILD_DIR/sources.list"
cp "$BASE_JAR" "$OUTPUT_JAR"
jar --update --date=2024-11-15T23:29:00Z --file "$OUTPUT_JAR" -C "$CLASS_DIR" .
mkdir -p "$SMOKE_CLASS_DIR"
find "$SMOKE_CLASS_DIR" -mindepth 1 -delete
find "$PATCH_DIR/smoke" -name '*.java' -type f | sort > "$BUILD_DIR/smoke-sources.list"
javac --release 17 -cp "$CLASS_DIR:$BASE_JAR" -d "$SMOKE_CLASS_DIR" @"$BUILD_DIR/smoke-sources.list"
jar --create --date=2024-11-15T23:29:00Z --file "$SMOKE_JAR" \
    --manifest "$PATCH_DIR/smoke/MANIFEST.MF" -C "$SMOKE_CLASS_DIR" .
echo "$OUTPUT_JAR"
