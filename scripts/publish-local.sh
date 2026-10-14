#!/usr/bin/env bash
# Publishes com.example:mylibrary to build/local-repo, no credentials needed.
set -euo pipefail
cd "$(dirname "$0")/.."

./gradlew "$@" :mylibrary:publishReleasePublicationToLocalRepoRepository

echo
echo "Published artifacts:"
find build/local-repo -type f | sort
