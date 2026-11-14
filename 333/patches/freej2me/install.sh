#!/bin/sh
set -eu

PATCH_DIR=$(CDPATH= cd -- "$(dirname -- "$0")" && pwd)
REPO_DIR=$(CDPATH= cd -- "$PATCH_DIR/../.." && pwd)
ACTIVE_JAR=${FREEJ2ME_ACTIVE_JAR:-"$REPO_DIR/Emus/JAVA/zulu17/bin/freej2me-sdl.jar"}
BACKUP_JAR="$ACTIVE_JAR.pre-virtual-keyboard.bak"

if [ -n "${FREEJ2ME_BUILD_JAR:-}" ]; then
    BUILD_JAR=$FREEJ2ME_BUILD_JAR
else
    "$PATCH_DIR/build.sh" >/dev/null
    BUILD_JAR="$PATCH_DIR/build/freej2me-sdl.jar"
fi

EXPECTED_BASE=$(awk 'NR == 1 { print $1 }' "$PATCH_DIR/BASE_JAR.sha256")
if [ -f "$BACKUP_JAR" ]; then
    BASE_TO_CHECK="$BACKUP_JAR"
else
    BASE_TO_CHECK="$ACTIVE_JAR"
fi
ACTUAL_BASE=$(sha256sum "$BASE_TO_CHECK" | awk '{ print $1 }')
if [ "$ACTUAL_BASE" != "$EXPECTED_BASE" ]; then
    echo "Refusing install: unknown baseline $ACTUAL_BASE" >&2
    exit 1
fi

jar tf "$BUILD_JAR" >/dev/null
MAIN_CLASS=$(unzip -p "$BUILD_JAR" META-INF/MANIFEST.MF | tr -d '\r' | awk -F': ' '$1 == "Main-Class" { print $2 }')
if [ "$MAIN_CLASS" != "org.recompile.freej2me.Anbu" ]; then
    echo "Refusing install: invalid Main-Class '$MAIN_CLASS'" >&2
    exit 1
fi

if [ ! -f "$BACKUP_JAR" ]; then
    cp "$ACTIVE_JAR" "$BACKUP_JAR"
fi

TEMP_JAR=$(mktemp "$ACTIVE_JAR.tmp.XXXXXX")
trap 'rm -f "$TEMP_JAR"' EXIT HUP INT TERM
cp "$BUILD_JAR" "$TEMP_JAR"
chmod --reference="$ACTIVE_JAR" "$TEMP_JAR"
mv -f "$TEMP_JAR" "$ACTIVE_JAR"
trap - EXIT HUP INT TERM

INSTALLED_HASH=$(sha256sum "$ACTIVE_JAR" | awk '{ print $1 }')
BUILD_HASH=$(sha256sum "$BUILD_JAR" | awk '{ print $1 }')
if [ "$INSTALLED_HASH" != "$BUILD_HASH" ]; then
    echo "Install verification failed" >&2
    exit 1
fi
echo "Installed $ACTIVE_JAR ($INSTALLED_HASH)"
