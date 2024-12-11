#! /bin/bash
# bash because of readarray

# Run WPI (for resource leaks) over the NJR test suite.

set -e
set -o verbose
set -o xtrace
export SHELLOPTS
echo "SHELLOPTS=${SHELLOPTS}"

SCRIPTDIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" >/dev/null 2>&1 && pwd )"
export ORG_GRADLE_PROJECT_useJdk17Compiler=true
source "$SCRIPTDIR"/clone-related.sh



source "$SCRIPTDIR"/test-njr-install.sh

WPI_STDOUT="$NJR"/wpi-stdout
mkdir -p "$WPI_STDOUT"
cd "$NJR"
readarray -d '' dirs < <(printf '%s\0' "$NJR"/final_dataset/* | sort -zV)
for dir in "${dirs[@]}"; do
  dir_basename="$(basename "$dir")"
  stdout_file="${WPI_STDOUT}/${dir_basename}-wpi-stdout.txt"
  timeout 900 "$SCRIPTDIR"/test-njr-one-wpi.sh "$dir" > "$stdout_file" 2>&1
  exit_status=$?
  if [[ $exit_status -eq 124 ]]; then
    echo "error: timed out; 1 errors" >> "$stdout_file"
  elif [[ $exit_status -ne 0 ]]; then
    echo "error: status $exit_status; 1 errors" >> "$stdout_file"
  else
    echo "Success: test-njr-one-wpi.sh $dir_basename" >> "$stdout_file"
  fi
done

if ! grep "error:" "$WPI_STDOUT" ; then
  echo "Errors found while running WPI on NJR; see above"
  exit 1
fi
