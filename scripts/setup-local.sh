#!/usr/bin/env bash
# Writes local.properties (sdk.dir) for the root build and the consumer build.
# Usage: ./scripts/setup-local.sh [/path/to/Android/Sdk]
#        ANDROID_HOME may be used instead of the argument.
set -euo pipefail
cd "$(dirname "$0")/.."

sdk="${1:-${ANDROID_HOME:-${ANDROID_SDK_ROOT:-}}}"
if [ -z "$sdk" ]; then
  echo "usage: $0 /path/to/Android/Sdk   (or export ANDROID_HOME=...)" >&2
  exit 1
fi
if [ ! -d "$sdk/platforms" ]; then
  echo "not an Android SDK: $sdk" >&2
  exit 1
fi

for dir in . consumer-app; do
  printf 'sdk.dir=%s\n' "$sdk" > "$dir/local.properties"
done
echo "local.properties written (sdk.dir=$sdk)"
