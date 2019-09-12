#!/usr/bin/env bash

# Delete all docker compose volumes
SCRIPT_PATH="$(cd "$(dirname "$0")"; pwd -P)"
DATA_PATH=$(readlink -f "$SCRIPT_PATH/../docker/data")
if [[ ! -e ${DATA_PATH} ]]; then
  echo "Data path: $DATA_PATH not found"
  exit 1
fi
read -p "Delete all the data in $DATA_PATH, Are you sure? " -n 1 -r
echo
if [[ ! $REPLY =~ ^[Yy]$ ]]
then
  exit 1
fi
set -x
sudo rm -rf "$DATA_PATH/"
{ set +x; } 2>/dev/null
{ set +x; } 2>/dev/null
echo "### -------------------------------------------------------------------"
echo -e "###\e[32m Docker compose data erased"

