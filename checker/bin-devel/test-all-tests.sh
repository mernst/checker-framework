#!/bin/bash

set -e
set -o verbose
set -o xtrace
export SHELLOPTS

export CHECKERFRAMEWORK=`readlink -f ${CHECKERFRAMEWORK:-.}`
echo "CHECKERFRAMEWORK=$CHECKERFRAMEWORK"

# For debugging
git branch
# In Azure if you requeue, `git branch` may output:  (HEAD detached at pull/4/merge)
# Maybe it's due to a rebase?
git branch -a
echo SYSTEM_PULLREQUEST_TARGETBRANCH=$SYSTEM_PULLREQUEST_TARGETBRANCH
echo SYSTEM_PULLREQUEST_SOURCEBRANCH=$SYSTEM_PULLREQUEST_SOURCEBRANCH
echo BASE_COMMIT=$BASE_COMMIT
echo BUILD_SOURCEVERSION=$BUILD_SOURCEVERSION
env

SCRIPTDIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" >/dev/null 2>&1 && pwd )"
source $SCRIPTDIR/build.sh ${BUILDJDK}



./gradlew allTests --console=plain --warning-mode=all --no-daemon
# Moved example-tests-nobuildjdk out of all tests because it fails in
# the release script because the newest maven artifacts are not published yet.
./gradlew :checker:exampleTests --console=plain --warning-mode=all --no-daemon
