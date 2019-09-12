#!/usr/bin/env bash
SERVER=${SERVER:-http://acme.docker.localhost}
NB=${NB:-5}
DEBUG=${DEBUG:-true}
SCRIPT_PATH="$(cd "$(dirname "$0")"; pwd -P)"
set -e

function create_batch() {
  echo "### -------------------------------------------------------------------"
  echo "### Create a batch of $NB messages"
  set -x
  curl -s -XPOST -H"Content-type: application/json" "$SERVER/batches?total=$NB" | tee /tmp/acme-command.txt | jq

  { set +x; } 2>/dev/null
  batchId=$(cat /tmp/acme-command.txt | jq ".id"| tr -d '"')
  [[ "null" == "$batchId" ]] && exit 1 || echo "found"
}

function get_batch_info() {
  echo "### -------------------------------------------------------------------"
  echo "### Get info for batch: $batchId"
  set -x
  curl -s -XGET "$SERVER/batches/$batchId" | jq
  { set +x; } 2>/dev/null
}


function append_message() {
  key=$(</dev/urandom tr -dc _A-Z-a-z-0-9 | head -c${1:-8})
  echo "### -------------------------------------------------------------------"
  echo "### Append a message $key to batch: $batchId"
  set -x
  curl -s -XPOST -H"Content-type: application/json" "$SERVER/batches/$batchId/append?debug=$DEBUG" -d $'{"key": "'${key}'", "duration": 1000}' | jq
  { set +x; } 2>/dev/null
}


# -------------------------------------------------------
# main
create_batch
get_batch_info
for i in $(seq "$NB")
do
  append_message
done
get_batch_info
echo "### Wait ${NB} seconds ..."
sleep ${NB}
get_batch_info

echo "### -------------------------------------------------------------------"
echo "### Test done"
