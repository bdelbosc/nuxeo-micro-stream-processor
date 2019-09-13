#!/usr/bin/env bash
ROOT_PATH="$(cd "$(dirname "$0")/.."; pwd -P)"
MAVEN_OPTS='-Xmx2g -Xms2g -XX:+TieredCompilation -XX:TieredStopAtLevel=1'
DURATION=${DURATION:100}
set -e
set -x
cd $ROOT_PATH/acme-simulation
mvn -nsu test gatling:test -Pbench -DnbMessages=10000 -DworkDurationMs=${DURATION} -DpauseMs=0 -Dusers=8
{ set +x; } 2>/dev/null
echo "### -------------------------------------------------------------------"
echo -e "###\e[32m Benchmark Done"
find $ROOT_PATH/acme-rest/target/gatling -name index.html | sort -r | head -1
