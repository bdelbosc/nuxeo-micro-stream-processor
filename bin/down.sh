#!/usr/bin/env bash
SCRIPT_PATH="$(cd "$(dirname "$0")"; pwd -P)"
set -e
set -x
cd ${SCRIPT_PATH}/../docker
docker-compose down --volume
{ set +x; } 2>/dev/null
echo "### -------------------------------------------------------------------"
echo -e "###\e[32m Docker compose down"
