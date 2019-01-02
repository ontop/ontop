#!/usr/bin/env bash

CURRENT_DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"

cd ${CURRENT_DIR}/../../../..
./mvnw clean install -DskipTests
cd build/distribution
../../mvnw assembly:assembly
cd target
unzip -o ontop-distr*.zip -d ontop
cd ontop
rm ontop ontop.bat ontop-completion.sh
docker build -t ghxiao/ontop-endpoint -f  ../../src/dockerfiles/Dockerfile .
cd ${CURRENT_DIR}
