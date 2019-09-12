#!/usr/bin/env bash
ROOT_PATH="$(cd "$(dirname "$0")/.."; pwd -P)"
MAVEN_OPTS='-Xmx2g -Xms2g -XX:+TieredCompilation -XX:TieredStopAtLevel=1'
set -e
set -x
cd $ROOT_PATH
mvn -nsu install -T4 -DskipTests=true
cd ./docker
docker-compose build
docker images | head -n4
{ set +x; } 2>/dev/null
echo "### -------------------------------------------------------------------"
echo -e "###\e[32m Build one"
