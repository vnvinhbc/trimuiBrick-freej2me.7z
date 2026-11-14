#!/bin/sh
set -eu

PATCH_DIR=$(CDPATH= cd -- "$(dirname -- "$0")" && pwd)
FIRST_JAR=$(mktemp /tmp/freej2me-build-first.XXXXXX.jar)
trap 'rm -f "$FIRST_JAR"' EXIT HUP INT TERM

"$PATCH_DIR/build.sh" >/dev/null
cp "$PATCH_DIR/build/freej2me-sdl.jar" "$FIRST_JAR"
sleep 1
"$PATCH_DIR/build.sh" >/dev/null

FIRST_HASH=$(sha256sum "$FIRST_JAR" | awk '{ print $1 }')
SECOND_HASH=$(sha256sum "$PATCH_DIR/build/freej2me-sdl.jar" | awk '{ print $1 }')
if [ "$FIRST_HASH" != "$SECOND_HASH" ]; then
    echo "non-reproducible build: $FIRST_HASH != $SECOND_HASH" >&2
    exit 1
fi
echo "reproducible build: $FIRST_HASH"
