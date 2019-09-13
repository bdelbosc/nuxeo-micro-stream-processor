#!/usr/bin/env bash
if [[ -z "$1" ]]; then
  echo "ERROR Missing container id"
  exit 1  
fi
CONTAINER=$1
FILE=${FILE:-record-00.jfr}
DURATION=${DURATION:-30}
echo "### -------------------------------------------------------------------"
echo "### Run flight record for ${DURATION}s on $CONTAINER"
set -x
docker exec -it ${CONTAINER} jcmd acme JFR.start duration=${DURATION}s filename=/tmp/${FILE}
sleep ${DURATION}
sleep 2
docker cp ${CONTAINER}:/tmp/${FILE} /tmp/${FILE}
{ set +x; } 2>/dev/null
echo "### -------------------------------------------------------------------"
echo -e "###\e[32m JFR: /tmp/$FILE"
