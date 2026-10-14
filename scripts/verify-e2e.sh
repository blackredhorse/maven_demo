#!/usr/bin/env bash
# End-to-end offline proof:
#   1. build + test + publish the AAR/JAR into build/local-repo
#   2. resolve that exact artifact from a separate Gradle build (consumer-app)
#   3. inspect what is inside the published AAR
set -euo pipefail
cd "$(dirname "$0")/.."

echo "==> [1/3] build, test and publish to build/local-repo"
./gradlew :mylibrary:testReleaseUnitTest :mylibrary:publishReleasePublicationToLocalRepoRepository

echo
echo "==> [2/3] published repository layout"
find build/local-repo -type f | sort

echo
echo "==> [3/3] consume the artifact from consumer-app"
./gradlew -p consumer-app assembleDebug

echo
echo "consumer APK/artifacts:"
find consumer-app/build/outputs -type f 2>/dev/null | sort || true

echo
echo "AAR content (classes.jar + resources prove it is a real library):"
unzip -l build/local-repo/com/example/mylibrary/1.0.0/mylibrary-1.0.0.aar | sed -n '1,20p'

if command -v mvn >/dev/null 2>&1; then
  echo
  echo "==> [4/4] Maven can resolve the same coordinates too"
  mvn -B -q dependency:get \
    -Dartifact="com.example:mylibrary:1.0.0:aar" \
    -DremoteRepositories="demo::default::file://$PWD/build/local-repo" \
    -Dmaven.repo.local="$PWD/build/m2-probe"
  echo "Maven resolved com.example:mylibrary:1.0.0 (aar)"
fi

echo
echo "OK - the published artifact resolves and compiles inside an Android app."
