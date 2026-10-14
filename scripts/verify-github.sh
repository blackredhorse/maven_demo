#!/usr/bin/env bash
# Verifies that the package published to GitHub Packages is really consumable.
#
#   ./scripts/verify-github.sh              # download + inspect + compile an app
#   ./scripts/verify-github.sh :probe:verifyArtifact   # only the download check
#
# Required environment variables:
#   GITHUB_ACTOR  your GitHub username
#   GITHUB_TOKEN  token with the `read:packages` scope (needed even for public packages)
set -euo pipefail
cd "$(dirname "$0")/.."

: "${GITHUB_ACTOR:?set GITHUB_ACTOR to your GitHub username}"
: "${GITHUB_TOKEN:?set GITHUB_TOKEN to a token with the read:packages scope}"

OWNER="${GITHUB_OWNER:-blackredhorse}"
REPO="${GITHUB_REPO:-maven_demo}"
VERSION="${LIBRARY_VERSION:-1.0.0}"
TARGETS=("${@:-:probe:verifyArtifact :app:assembleDebug}")

echo "==> resolving com.example:mylibrary:${VERSION} from"
echo "    https://maven.pkg.github.com/${OWNER}/${REPO}"

./gradlew -p consumer-github \
  --refresh-dependencies \
  -Pgithub.owner="$OWNER" \
  -Pgithub.repo="$REPO" \
  -Plibrary.version="$VERSION" \
  "${TARGETS[@]}"

echo
echo "OK - the package is downloadable and usable from GitHub Packages."
