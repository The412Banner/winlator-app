#!/usr/bin/env bash
# make_wcp.sh — package upstream releases into Winlator .wcp content packages
#
# Usage:
#   ./tools/make_wcp.sh dxvk    <version> <upstream.tar.gz>
#   ./tools/make_wcp.sh vkd3d   <version> <upstream.tar.zst>
#   ./tools/make_wcp.sh box64   <version> <box64-binary>
#   ./tools/make_wcp.sh wine    <version> <wine-dir-or-tar>
#   ./tools/make_wcp.sh wowbox64 <version> <wowbox64.dll>
#   ./tools/make_wcp.sh fexcore  <version> <dir-with-dlls>
#
# Output: <type>-<version>.wcp in current directory
#
# Requirements: tar, zstd, python3

set -euo pipefail

TYPE="${1:-}"
VERSION="${2:-}"
INPUT="${3:-}"

die() { echo "ERROR: $*" >&2; exit 1; }

[[ -n "$TYPE" && -n "$VERSION" && -n "$INPUT" ]] || {
    sed -n '2,12p' "$0" | grep -v '^$'
    exit 1
}

[[ -e "$INPUT" ]] || die "Input not found: $INPUT"

WORK="$(mktemp -d)"
trap 'rm -rf "$WORK"' EXIT

STAGE="$WORK/stage"
mkdir -p "$STAGE"

# ── helpers ──────────────────────────────────────────────────────────────────

write_profile() {
    python3 -c "
import json, sys
data = $1
print(json.dumps(data, indent=2))
" > "$STAGE/profile.json"
}

pack_wcp() {
    local out="${TYPE}-${VERSION}.wcp"
    echo "→ packing $out"
    tar -C "$STAGE" -c . | zstd -19 -q -o "$out"
    echo "✓ $(du -sh "$out" | cut -f1)  $out"
}

# ── DXVK ─────────────────────────────────────────────────────────────────────
# Upstream: dxvk-X.Y.Z.tar.gz  (x32/ and x64/ inside a top-level dir)

pack_dxvk() {
    local extract="$WORK/dxvk_src"
    mkdir -p "$extract"
    tar -xzf "$INPUT" -C "$extract" --strip-components=1

    mkdir -p "$STAGE/system32" "$STAGE/syswow64"

    # x64 → system32, x32 → syswow64
    local dlls=(d3d8.dll d3d9.dll d3d10.dll d3d10_1.dll d3d10core.dll d3d11.dll dxgi.dll)
    for dll in "${dlls[@]}"; do
        [[ -f "$extract/x64/$dll" ]] && cp "$extract/x64/$dll" "$STAGE/system32/$dll"
        [[ -f "$extract/x32/$dll" ]] && cp "$extract/x32/$dll" "$STAGE/syswow64/$dll"
    done

    local files_json='[]'
    files_json=$(python3 -c "
import os, json
files = []
for d, dest in [('system32', '\${system32}'), ('syswow64', '\${syswow64}')]:
    for f in sorted(os.listdir('$STAGE/' + d)):
        files.append({'source': d + '/' + f, 'target': dest + '/' + f})
print(json.dumps(files))
")

    write_profile "{
        'type': 'DXVK',
        'versionName': '$VERSION',
        'versionCode': 1,
        'description': 'DXVK $VERSION — https://github.com/doitsujin/dxvk',
        'files': $files_json
    }"
    pack_wcp
}

# ── VKD3D-Proton ──────────────────────────────────────────────────────────────
# Upstream: vkd3d-proton-X.Y.tar.zst  (x86/ and x64/ inside a top-level dir)

pack_vkd3d() {
    local extract="$WORK/vkd3d_src"
    mkdir -p "$extract"
    zstd -d < "$INPUT" | tar -x -C "$extract" --strip-components=1

    mkdir -p "$STAGE/system32" "$STAGE/syswow64"

    local dlls=(d3d12.dll d3d12core.dll)
    for dll in "${dlls[@]}"; do
        [[ -f "$extract/x64/$dll" ]] && cp "$extract/x64/$dll" "$STAGE/system32/$dll"
        [[ -f "$extract/x86/$dll" ]] && cp "$extract/x86/$dll" "$STAGE/syswow64/$dll"
    done

    write_profile "{
        'type': 'VKD3D',
        'versionName': '$VERSION',
        'versionCode': 1,
        'description': 'VKD3D-Proton $VERSION — https://github.com/HansKristian-Work/vkd3d-proton',
        'files': [
            {'source': 'system32/d3d12.dll',     'target': '\${system32}/d3d12.dll'},
            {'source': 'system32/d3d12core.dll',  'target': '\${system32}/d3d12core.dll'},
            {'source': 'syswow64/d3d12.dll',      'target': '\${syswow64}/d3d12.dll'},
            {'source': 'syswow64/d3d12core.dll',  'target': '\${syswow64}/d3d12core.dll'}
        ]
    }"
    pack_wcp
}

# ── Box64 ─────────────────────────────────────────────────────────────────────
# Input: the box64 ARM64 binary (just the executable)

pack_box64() {
    cp "$INPUT" "$STAGE/box64"
    chmod +x "$STAGE/box64"

    write_profile "{
        'type': 'Box64',
        'versionName': '$VERSION',
        'versionCode': 1,
        'description': 'Box64 $VERSION — https://github.com/ptitSeb/box64',
        'files': [
            {'source': 'box64', 'target': '\${bindir}/box64'}
        ]
    }"
    pack_wcp
}

# ── Wine ──────────────────────────────────────────────────────────────────────
# Input: a directory OR tarball containing bin/ lib/ and prefixPack.txz
# If it's a tarball, we extract it first.

pack_wine() {
    local src="$INPUT"

    if [[ -f "$INPUT" ]]; then
        local wext="$WORK/wine_src"
        mkdir -p "$wext"
        case "$INPUT" in
            *.tar.xz|*.txz) xz -d < "$INPUT" | tar -x -C "$wext" --strip-components=1 ;;
            *.tar.zst)       zstd -d < "$INPUT" | tar -x -C "$wext" --strip-components=1 ;;
            *.tar.gz)        tar -xzf "$INPUT" -C "$wext" --strip-components=1 ;;
            *) die "Unsupported Wine archive format: $INPUT" ;;
        esac
        src="$wext"
    fi

    [[ -d "$src/bin" ]] || die "Wine source missing bin/"
    [[ -d "$src/lib" ]] || die "Wine source missing lib/"

    cp -r "$src/bin" "$STAGE/bin"
    cp -r "$src/lib" "$STAGE/lib"
    [[ -f "$src/prefixPack.txz" ]] && cp "$src/prefixPack.txz" "$STAGE/prefixPack.txz"

    local prefix_pack="prefixPack.txz"
    [[ -f "$STAGE/prefixPack.txz" ]] || prefix_pack=""

    write_profile "{
        'type': 'Wine',
        'versionName': '$VERSION',
        'versionCode': 1,
        'description': 'Wine $VERSION',
        'files': [],
        'wine': {
            'binPath': 'bin',
            'libPath': 'lib',
            'prefixPack': '$prefix_pack'
        }
    }"
    pack_wcp
}

# ── WOWBox64 ──────────────────────────────────────────────────────────────────
# Input: the wowbox64.dll file

pack_wowbox64() {
    cp "$INPUT" "$STAGE/wowbox64.dll"

    write_profile "{
        'type': 'WOWBox64',
        'versionName': '$VERSION',
        'versionCode': 1,
        'description': 'WOWBox64 $VERSION',
        'files': [
            {'source': 'wowbox64.dll', 'target': '\${system32}/wowbox64.dll'}
        ]
    }"
    pack_wcp
}

# ── FEXCore ───────────────────────────────────────────────────────────────────
# Input: directory containing libwow64fex.dll and/or libarm64ecfex.dll

pack_fexcore() {
    local files_json='[]'
    local found=0

    for dll in libwow64fex.dll libarm64ecfex.dll; do
        if [[ -f "$INPUT/$dll" ]]; then
            cp "$INPUT/$dll" "$STAGE/$dll"
            found=$((found+1))
        fi
    done
    [[ $found -gt 0 ]] || die "No FEXCore DLLs found in $INPUT"

    files_json=$(python3 -c "
import os, json
files = []
for dll in ['libwow64fex.dll', 'libarm64ecfex.dll']:
    if os.path.exists('$STAGE/' + dll):
        files.append({'source': dll, 'target': '\${system32}/' + dll})
print(json.dumps(files))
")

    write_profile "{
        'type': 'FEXCore',
        'versionName': '$VERSION',
        'versionCode': 1,
        'description': 'FEXCore $VERSION — https://github.com/FEX-Emu/FEX',
        'files': $files_json
    }"
    pack_wcp
}

# ── dispatch ──────────────────────────────────────────────────────────────────

case "$TYPE" in
    dxvk)     pack_dxvk ;;
    vkd3d)    pack_vkd3d ;;
    box64)    pack_box64 ;;
    wine)     pack_wine ;;
    wowbox64) pack_wowbox64 ;;
    fexcore)  pack_fexcore ;;
    *) die "Unknown type '$TYPE'. Valid: dxvk vkd3d box64 wine wowbox64 fexcore" ;;
esac
