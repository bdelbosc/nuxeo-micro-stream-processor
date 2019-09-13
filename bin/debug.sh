#!/usr/bin/env bash
if [[ -z "$1" ]]; then
  echo "ERROR Missing container id"
  exit 1  
fi
CONTAINER=$1
PORT=${PORT:-8787}
echo "### -------------------------------------------------------------------"
echo "### Export port: $PORT from container: $CONTAINER to localhost:$PORT"
CONTAINER_IP=$(docker inspect -f "{{range .NetworkSettings.Networks}}{{.IPAddress}}{{end}}" $CONTAINER)
[[ -z "$CONTAINER_IP" ]] && exit 1
set -x
docker run --rm --net host alpine/socat TCP-LISTEN:$PORT,fork TCP-CONNECT:$CONTAINER_IP:$PORT
