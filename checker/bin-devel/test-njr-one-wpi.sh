#!/bin/bash

# This script runs WPI on one NJR project.
# It takes two arguments:
#  * the checker to run
#  * the absolute path to an NJR project

echo "entering $(basename "${BASH_SOURCE[0]}") $*"

set -e
set -o verbose
set -o xtrace
export SHELLOPTS
echo "SHELLOPTS=${SHELLOPTS}"

if [ "$#" -eq 0 ]; then
    echo "$(basename "$0"): wrong number of arguments $#: $*"
    exit 2
fi

checker=$1
PROJECT_PATH=$2
# PROJECT=$(basename $PROJECT_PATH)

export ORG_GRADLE_PROJECT_useJdk17Compiler=true
SCRIPTDIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" >/dev/null 2>&1 && pwd )"

# A list of the absolute paths of the source files.
SOURCES_FILE="$PROJECT_PATH/cf_sources.txt"
if [ ! -f "$SOURCES_FILE" ] ; then
  find "$PROJECT_PATH"/src -name "*.java" > "$SOURCES_FILE"
fi

# Compile the program so that WPI_OUT_DIR is created.
"$SCRIPTDIR"/test-njr-one-javac.sh "$checker" "$PROJECT_PATH"

CLEAN_CMD="rm -rf ./classes"

# Where should the output be placed at the end? This directory is also
# used to store intermediate WPI results. The directory does not need to
# exist. If it does exist when this script starts, it will be deleted.
# If you are using the subprojects script, set WPI_TEMP_DIR to "$1".
WPI_TEMP_DIR=$PROJECT_PATH/wpi-temp
# Where is WPI's output placed by the Checker Framework? This is some
# directory ending in build/whole-program-inference. For most projects,
# this directory is just ./build/whole-program-inference .
# The example in this script is the output directory when running via the gradle plugin.
# (The CF automatically puts all WPI outputs in ./build/whole-program-inference,
# where . is the directory from which the javac command was invoked (ie, javac's
# working directory). In many build systems (e.g., Maven), that directory would be the project.
# But, some build systems, such as Gradle, cache build outputs in a central location
# per-machine, and as part of that it runs its builds from that central location.)
# The directory to use here might vary between build systems, between machines
# (e.g., depending on your local Gradle settings), and even between projects using the
# same build system (e.g., because of a project's settings.gradle file).

# Program needs to compiled before running script so WPI creates this directory.
# If you are using the subprojects script, set WPI_OUT_DIR to "$2".
WPI_OUT_DIR="$PROJECT_PATH"/build/whole-program-inference

# Whether to run in debug mode. In debug mode, output is printed to the terminal
# at the beginning of each iteration, and the diff between each pair of iterations is
# saved in a file named iteration$count.diff, starting with iteration1.diff.
# (Note that these files are overwritten if they already exist.)
DEBUG=1

# End of variables. You probably don't need to make changes below this line.

rm -rf "${WPI_TEMP_DIR}"
mkdir -p "${WPI_TEMP_DIR}"

# Store all the intermediate ajava files for each iteration.
WPI_ITERATION_OUTPUTS="$PROJECT_PATH"/wpi-iterations
rm -rf "${WPI_ITERATION_OUTPUTS}"
mkdir -p "${WPI_ITERATION_OUTPUTS}"

# Starts at 1
iteration_number=$(find . -type f -maxdepth 1 -printf x | wc -c)
((iteration_number++))

while : ; do
    if [[ ${DEBUG} == 1 ]]; then
    SECONDS=0
	echo "entering iteration ${iteration_number}"
    fi
    "$SCRIPTDIR"/test-njr-one-javac.sh "$checker" "$PROJECT_PATH"
    ${CLEAN_CMD}
    # This mkdir is needed when the project has subprojects.
    mkdir -p "${WPI_TEMP_DIR}"
    mkdir -p "${WPI_OUT_DIR}"
    DIFF_RESULT=$(diff -r "${WPI_TEMP_DIR}" "${WPI_OUT_DIR}" || true)
    if [[ ${DEBUG} == 1 ]]; then
	echo "putting the diff for iteration $iteration_number into $(realpath iteration$iteration_number.diff)"
	echo "${DIFF_RESULT}" > "iteration${iteration_number}.diff"
    fi
    [[ "$DIFF_RESULT" != "" ]] || break
    rm -rf "${WPI_TEMP_DIR}"
    mv "${WPI_OUT_DIR}" "${WPI_TEMP_DIR}"
    # Also store the intermediate WPI results
    mkdir -p "${WPI_ITERATION_OUTPUTS}/iteration${iteration_number}"
    cp -rf "${WPI_TEMP_DIR}"/* "${WPI_ITERATION_OUTPUTS}/iteration${iteration_number}"
    echo "ending iteration ${iteration_number}, time taken: $SECONDS seconds"
    echo
    ((iteration_number++))
done
