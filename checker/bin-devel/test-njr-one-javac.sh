#! /bin/sh

# This script runs javac once, on one particular NJR project.
# It takes two arguments:
#  * the checker to run; for example, org.checkerframework.checker.resourceleak.ResourceLeakChecker
#  * the absolute path to an NJR project


if [ "$#" -eq 0 ]; then
    echo "$(basename "$0"): needs 2 arguments, got $#: $*"
    exit 2
fi

checker=$1
PROJECT_PATH=$2
SOURCES_FILE="$PROJECT_PATH/cf_sources.txt"

# A list of the absolute paths of the source files.
if [ ! -f "$SOURCES_FILE" ] ; then
  find "$PROJECT_PATH"/src -name "*.java" > "$SOURCES_FILE"
fi

#-J-agentlib:jdwp=transport=dt_socket,server=y,suspend=y,address=5005 \
#-AenableReturnsReceiverForRlc

# Do not quote this variable when using it.
JAVA11_add_exports="-J--add-exports=jdk.compiler/com.sun.tools.javac.api=ALL-UNNAMED -J--add-exports=jdk.compiler/com.sun.tools.javac.code=ALL-UNNAMED -J--add-exports=jdk.compiler/com.sun.tools.javac.file=ALL-UNNAMED -J--add-exports=jdk.compiler/com.sun.tools.javac.main=ALL-UNNAMED -J--add-exports=jdk.compiler/com.sun.tools.javac.model=ALL-UNNAMED -J--add-exports=jdk.compiler/com.sun.tools.javac.processing=ALL-UNNAMED -J--add-exports=jdk.compiler/com.sun.tools.javac.tree=ALL-UNNAMED -J--add-exports=jdk.compiler/com.sun.tools.javac.util=ALL-UNNAMED -J--add-opens=jdk.compiler/com.sun.tools.javac.comp=ALL-UNNAMED"

#shellcheck disable=SC2086
(cd "$CHECKERFRAMEWORK" && ./gradlew -q assembleforjavac) && \
javac \
$JAVA11_add_exports \
-processorpath "$CHECKERFRAMEWORK"/checker/dist/checker.jar \
-processor "$checker" \
-Adetailedmsgtext \
-Aajava="$PROJECT_PATH"/wpi-out \
-Ainfer=ajava \
-Awarns \
-Xmaxwarns 10000 \
-AshowPrefixInWarningMessages \
-J-Xmx32G \
-J-ea \
-g \
-d "$PROJECT_PATH"/classes \
-cp "$PROJECT_PATH"/lib:"$CHECKERFRAMEWORK"/checker/dist/checker-qual.jar \
@"${SOURCES_FILE}"
