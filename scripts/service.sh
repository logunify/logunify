#!/bin/bash
set -e

while true; do
  case "$1" in
    --image)
      IMAGE=$2
      shift
      shift
      ;;
  -s | --schema-path)
    SCHEMA_PATH=$2
    shift
    shift
    ;;
  -c | --config-file)
    CONFIG_FILE_PATH=$2
    shift
    shift
    ;;
  *)
    break
    ;;
  esac
done

if [ -z "$IMAGE" ]; then
  IMAGE="logunify/logunify:latest"
fi
if [ -z "$SCHEMA_PATH" ]; then
  echo "Please provide path to schemas by -s or --schema-path"
  exit 1
fi
if [ -z "$CONFIG_FILE_PATH" ]; then
  echo "No config file provided, using the default config."
fi
if [[ "$SCHEMA_PATH" != /* && "$SCHEMA_PATH" != ~* ]]; then
  SCHEMA_PATH="$(pwd)/$SCHEMA_PATH"
fi
if [[ -n "$CONFIG_FILE_PATH" && "$CONFIG_FILE_PATH" != /* && "$CONFIG_FILE_PATH" != ~* ]]; then
  CONFIG_FILE_PATH="$(pwd)/$CONFIG_FILE_PATH"
fi
if [ ! -d "$SCHEMA_PATH" ]; then
  echo "Schema path $SCHEMA_PATH does not exist."
  exit 1
fi
if [[ -n "$CONFIG_FILE_PATH" && ! -f "$CONFIG_FILE_PATH" ]]; then
  echo "Config file $CONFIG_FILE_PATH does not exist."
  exit 1
fi

CONTAINER_NAME="logunify"
docker kill "$CONTAINER_NAME" > /dev/null 2>&1 || docker rm "$CONTAINER_NAME" > /dev/null 2>&1 || true
docker run --name "$CONTAINER_NAME" -p 8081:8081 -it -v "$CONFIG_FILE_PATH":/app/config.properties -v "$SCHEMA_PATH":/app/schemas "$@" --rm "$IMAGE"
