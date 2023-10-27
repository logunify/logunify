#!/bin/bash
set -e

GRADLE_CLEAN_OPTION=''
while true; do
  case "$1" in
  -c | --clean)
    GRADLE_CLEAN_OPTION='clean'
    shift
    ;;
  *) break ;;
  esac
done

./gradlew $GRADLE_CLEAN_OPTION :web_service:build :codegen:installDist
docker build --target logunify -t logunify/logunify:local .
docker build --target codegen -t logunify/codegen:local .
