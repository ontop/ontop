#!/usr/bin/env bash

set -e

CURRENT_DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"

ONTOP_HOME=${CURRENT_DIR}/../..

cd ${ONTOP_HOME}
${ONTOP_HOME}/mvnw clean package -Pcli
cd build/distribution/target
rm -rf ontop
unzip -o ontop-cli*.zip -d ontop
cd ontop
rm -r ontop.bat ontop ontop-completion.sh jdbc
cp ${ONTOP_HOME}/client/docker/entrypoint.sh .
docker build --no-cache -t ontop/ontop-endpoint:4.3.0-SNAPSHOT -f ${ONTOP_HOME}/client/docker/Dockerfile .
cd ${CURRENT_DIR}
