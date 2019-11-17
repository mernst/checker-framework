#!/bin/bash

set -e
set -o verbose
set -o xtrace
export SHELLOPTS

git -C /tmp/plume-scripts pull > /dev/null 2>&1 \
  || git -C /tmp clone --depth 1 -q https://github.com/plume-lib/plume-scripts.git
eval `/tmp/plume-scripts/ci-info typetools`

export CHECKERFRAMEWORK="${CHECKERFRAMEWORK:-$(pwd -P)}"
echo "CHECKERFRAMEWORK=$CHECKERFRAMEWORK"

SCRIPTDIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" >/dev/null 2>&1 && pwd )"
source $SCRIPTDIR/build.sh ${BUILDJDK}

# Documentation
./gradlew javadocPrivate --console=plain --warning-mode=all --no-daemon

# This comes last, in case we wish to ignore it
# if [ "$CI_IS_PR" == "true" ] ; then
(./gradlew requireJavadocPrivate --console=plain --warning-mode=all --no-daemon > /tmp/warnings.txt 2>&1) || true
/tmp/plume-scripts/ci-lint-diff --debug /tmp/warnings.txt
