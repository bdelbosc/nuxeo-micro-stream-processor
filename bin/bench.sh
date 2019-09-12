#!/usr/bin/env bash
ROOT_PATH="$(cd "$(dirname "$0")/.."; pwd -P)"
MAVEN_OPTS='-Xmx2g -Xms2g -XX:+TieredCompilation -XX:TieredStopAtLevel=1'
set -e
set -x
cd $ROOT_PATH/acme-rest
mvn -nsu test gatling:test -Pbench -DnbMessages=10000
{ set +x; } 2>/dev/null
echo "### -------------------------------------------------------------------"
echo "### Benchmark Done"
find $ROOT_PATH/acme-rest/target/gatling -name index.html | sort -r | head -1
