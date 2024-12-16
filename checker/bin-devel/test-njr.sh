#! /bin/bash
# bash because of readarray

# Run WPI (for resource leaks) over the NJR test suite.
# The optional argument can be "part1", "part2", ..., "part8".

# Don't halt on all errors.
# set -e

set -o verbose
set -o xtrace
export SHELLOPTS
echo "SHELLOPTS=${SHELLOPTS}"

part=$1
checker=org.checkerframework.checker.resourceleak.ResourceLeakChecker

SCRIPTDIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" >/dev/null 2>&1 && pwd )"
export ORG_GRADLE_PROJECT_useJdk17Compiler=true
source "$SCRIPTDIR"/clone-related.sh



source "$SCRIPTDIR"/test-njr-install.sh

WPI_STDOUT="$NJR"/wpi-stdout
mkdir -p "$WPI_STDOUT"
cd "$NJR"
readarray -d '' dirs < <(printf '%s\0' "$NJR"/final_dataset/* | sort -z)

case $part in
  "part1") readarray -d '' dirs < <(printf '%s\0' "$NJR"/final_dataset/url[0-1]* | sort -z) ;;
  "part2") readarray -d '' dirs < <(printf '%s\0' "$NJR"/final_dataset/url[2-3]* | sort -z) ;;
  "part3") readarray -d '' dirs < <(printf '%s\0' "$NJR"/final_dataset/url[4-5]* | sort -z) ;;
  "part4") readarray -d '' dirs < <(printf '%s\0' "$NJR"/final_dataset/url[6-7]* | sort -z) ;;
  "part5") readarray -d '' dirs < <(printf '%s\0' "$NJR"/final_dataset/url[8-9]* | sort -z) ;;
  "part6") readarray -d '' dirs < <(printf '%s\0' "$NJR"/final_dataset/url[a-b]* | sort -z) ;;
  "part7") readarray -d '' dirs < <(printf '%s\0' "$NJR"/final_dataset/url[c-d]* | sort -z) ;;
  "part8") readarray -d '' dirs < <(printf '%s\0' "$NJR"/final_dataset/url[e-f]* | sort -z) ;;
  *)       readarray -d '' dirs < <(printf '%s\0' "$NJR"/final_dataset/*         | sort -z) ;;
esac

for dir in "${dirs[@]}"; do
  dir_basename="$(basename "$dir")"
  stdout_file="${WPI_STDOUT}/${dir_basename}-wpi-stdout.txt"
  timeout 900 "$SCRIPTDIR"/test-njr-one-wpi.sh "$checker" "$dir" 2>&1 | tee "$stdout_file"
  exit_status=$?
  if [[ $exit_status -eq 124 ]]; then
    echo "error: timed out; 1 errors" >> "$stdout_file"
  elif [[ $exit_status -ne 0 ]]; then
    echo "error: status $exit_status; 1 errors" >> "$stdout_file"
  else
    echo "Success: test-njr-one-wpi.sh $checker $dir_basename" >> "$stdout_file"
  fi
done

if ! grep "error:" "$WPI_STDOUT" ; then
  #shellcheck disable=SC2046
  cat $(grep -l "error:" "$WPI_STDOUT")
  grep "error:" "$WPI_STDOUT"
  echo "Errors found while running WPI on NJR; see above"
  exit 1
fi