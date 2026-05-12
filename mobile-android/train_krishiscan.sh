#!/usr/bin/env bash
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
WORK_DIR="${WORK_DIR:-"$SCRIPT_DIR/training-work"}"
SKIP_INSTALL="${SKIP_INSTALL:-0}"
COPY_TO_ANDROID_ASSETS="${COPY_TO_ANDROID_ASSETS:-0}"

cd "$SCRIPT_DIR"

if [[ "$SKIP_INSTALL" != "1" ]]; then
  python3 -m pip install -r requirements-training.txt
fi

args=(train_krishiscan.py --work-dir "$WORK_DIR")
if [[ "$COPY_TO_ANDROID_ASSETS" == "1" ]]; then
  args+=(--copy-to-android-assets)
fi

python3 "${args[@]}" "$@"
