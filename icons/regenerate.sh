#!/usr/bin/env bash
# Regenerates rectify.icns / .ico / .png from rectify.svg. Run from anywhere; macOS only.
#
# Why a Swift renderer and not something off the shelf:
#   - `qlmanage` composites onto an OPAQUE WHITE ground and anchors the art top-left at small
#     sizes. An app icon needs transparent corners, so its output is unusable here.
#   - `cairosvg` is installed on this machine but its import is broken (Homebrew python3.14
#     ships a pyexpat built against the wrong libexpat).
#   - rsvg-convert / Inkscape / ImageMagick are not installed.
# AppKit's NSImage reads SVG and keeps alpha, and it is already on every Mac.
set -euo pipefail
cd "$(dirname "$0")"

SVG="rectify.svg"
SET="$(mktemp -d)/rectify.iconset"
mkdir -p "$SET"

# name:pixels — the 10 entries `iconutil` expects.
for spec in 16:icon_16x16 32:icon_16x16@2x 32:icon_32x32 64:icon_32x32@2x \
            128:icon_128x128 256:icon_128x128@2x 256:icon_256x256 \
            512:icon_256x256@2x 512:icon_512x512 1024:icon_512x512@2x; do
  swift render-svg.swift "$SVG" "$SET/${spec##*:}.png" "${spec%%:*}"
done

iconutil -c icns "$SET" -o rectify.icns
cp "$SET/icon_512x512.png" rectify.png

# ICO tops out at 256, and PIL only downscales — so the 256 render is the base.
python3 - "$SET" <<'PY'
import sys
from PIL import Image
base = Image.open(f"{sys.argv[1]}/icon_256x256.png").convert("RGBA")
base.save("rectify.ico", format="ICO",
          sizes=[(16, 16), (32, 32), (48, 48), (64, 64), (128, 128), (256, 256)])
PY

echo "regenerated: rectify.icns rectify.ico rectify.png"
