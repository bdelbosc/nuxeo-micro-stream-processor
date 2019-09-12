#!/usr/bin/env bash
SCRIPT_PATH="$(cd "$(dirname "$0")"; pwd -P)"
MAVEN_OPTS='-Xmx2g -Xms2g -XX:+TieredCompilation -XX:TieredStopAtLevel=1'
set -e
set -x
mvn -nsu install -T4 -DskipTests=true
cd ${SCRIPT_PATH}/../docker
docker-compose build
docker images | head -n4
{ set +x; } 2>/dev/null
echo "### -------------------------------------------------------------------"
echo "### Build Done"
