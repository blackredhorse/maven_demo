#!/usr/bin/env bash
# Publishes com.example:mylibrary to GitHub Packages.
#
# Required environment variables:
#   GITHUB_OWNER  GitHub user or organization that owns the package
#   GITHUB_REPO   repository the package is published to
#   GITHUB_ACTOR  your GitHub username
#   GITHUB_TOKEN  PAT with the `write:packages` scope
set -euo pipefail
cd "$(dirname "$0")/.."

: "${GITHUB_OWNER:?set GITHUB_OWNER, e.g. my-name}"
: "${GITHUB_REPO:?set GITHUB_REPO, e.g. maven-demo-github}"
: "${GITHUB_ACTOR:?set GITHUB_ACTOR to your GitHub username}"
: "${GITHUB_TOKEN:?set GITHUB_TOKEN to a PAT with the write:packages scope}"

./gradlew "$@" \
  -Pgithub.owner="$GITHUB_OWNER" \
  -Pgithub.repo="$GITHUB_REPO" \
  -Plibrary.version="${LIBRARY_VERSION:-1.0.0}" \
  :mylibrary:publishReleasePublicationToGitHubRepository

echo
echo "Done: https://github.com/${GITHUB_OWNER}/${GITHUB_REPO}/pkgs"
