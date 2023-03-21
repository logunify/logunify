#!/bin/bash
set -e

while true; do
  case "$1" in
  -c | --clean)
    GRADLE_CLEAN_OPTION='-c'
    shift
    ;;
  *)
    break
    ;;
  esac
done

SCRIPT_DIR=$(cd -- "$(dirname -- "${BASH_SOURCE[0]}")" &>/dev/null && pwd) &&
  "$SCRIPT_DIR"/build.sh $GRADLE_CLEAN_OPTION

SCRIPT_DIR=$(cd -- "$(dirname -- "${BASH_SOURCE[0]}")" &>/dev/null && pwd) &&
  "$SCRIPT_DIR"/codegen.sh --image logunify/codegen:local "$@"
