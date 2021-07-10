#!/bin/bash

set -e
set -o verbose
set -o xtrace
export SHELLOPTS
echo "SHELLOPTS=${SHELLOPTS}"

SCRIPTDIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" >/dev/null 2>&1 && pwd )"
# shellcheck disable=SC1090# In newer shellcheck than 0.6.0, pass: "-P SCRIPTDIR" (literally)
source "$SCRIPTDIR"/build.sh



./gradlew test --console=plain --warning-mode=all --no-daemon

# TODO: Running a separate Gradle job is a hack, there should be a gradle
# command-line argument such as -PrunNullnessJspecifySamplesTest.
# TODO: When the tests all pass, eliminate this and remove the `exclude`
# directive in checker/build.gradle .
if type -p java; then
  _java=java
elif [[ -n "$JAVA_HOME" ]] && [[ -x "$JAVA_HOME/bin/java" ]];  then
  _java="$JAVA_HOME/bin/java"
else
  echo "Can't find java"
  exit 1
fi
version=$("$_java" -version 2>&1 | head -1 | cut -d'"' -f2 | sed '/^1\./s///' | cut -d'.' -f1)
if [[ "$version" -ge 9 ]]; then
  ./gradlew NullnessJspecifySamplesTest
fi
