#!/bin/sh
set -eu

PATCH_DIR=$(CDPATH= cd -- "$(dirname -- "$0")" && pwd)
STALE_DIR="$PATCH_DIR/build/classes/stale"
STALE_CLASS="$STALE_DIR/Removed.class"
mkdir -p "$STALE_DIR"
printf 'stale\n' > "$STALE_CLASS"
"$PATCH_DIR/build.sh" >/dev/null
if jar tf "$PATCH_DIR/build/freej2me-sdl.jar" | grep -q '^stale/Removed.class$'; then
    echo "build retained stale class" >&2
    exit 1
fi
echo "clean-build: PASS"
