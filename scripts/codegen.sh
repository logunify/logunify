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
  -d | --destination-path)
    DESTINATION_PATH=$2
    shift
    shift
    ;;
  *)
    break
    ;;
  esac
done

if [ -z "$IMAGE" ]; then
  IMAGE="logunify/codegen:latest"
fi

if [ -z "$SCHEMA_PATH" ]; then
  echo "Please provide path to schemas by -s or --schema-path"
  exit 1
fi
if [[ "$SCHEMA_PATH" != /* && "$SCHEMA_PATH" != ~* ]]; then
  SCHEMA_PATH="$(pwd)/$SCHEMA_PATH"
fi
if [ ! -d "$SCHEMA_PATH" ] && [ ! -f "$SCHEMA_PATH" ]; then
  echo "Schema path $SCHEMA_PATH does not exist."
  exit 1
fi
if [ -d "$SCHEMA_PATH" ]; then
  MOUNTING_DESTINATION="/app/schemas"
  else
    MOUNTING_DESTINATION="/app/schemas/$(basename "$SCHEMA_PATH")"
fi

if [ -z "$DESTINATION_PATH" ]; then
  echo "Please provide path to generated files by -d or --destination-path"
  exit 1
fi
if [[ "$DESTINATION_PATH" != /* && "$DESTINATION_PATH" != ~* ]]; then
  DESTINATION_PATH="$(pwd)/$DESTINATION_PATH"
fi
if [ ! -d "$DESTINATION_PATH" ]; then
  echo "Destination path $DESTINATION_PATH does not exist."
  exit 1
fi

CONTAINER_NAME="logunify-codegen"
docker kill "$CONTAINER_NAME" > /dev/null 2>&1 || docker rm "$CONTAINER_NAME" > /dev/null 2>&1 || true
docker run --name "$CONTAINER_NAME" -it -v "$SCHEMA_PATH":"$MOUNTING_DESTINATION" -v "$DESTINATION_PATH":/app/generated --rm "$IMAGE" -- \
  -s /app/schemas \
  --logunify_plugin_path /app/codegen/bin/protoc_plugin \
  --destination_path /app/generated \
  "$@"
