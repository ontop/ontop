#!/bin/bash

# Usage: ./obdalib-core-test.sh

cd $ONTOP_BUILD_PATH/obdalib-core
mvn clean
mvn site
rm -rf $ONTOP_REPORT_PATH/obdalib-core
cp -R target/site $ONTOP_REPORT_PATH/obdalib-core
