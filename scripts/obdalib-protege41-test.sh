#!/bin/bash

# Usage: ./quest-sesame-test.sh 

cd $ONTOP_BUILD_PATH/obdalib-protege41
mvn clean
mvn site
rm -rf $ONTOP_REPORT_PATH/obdalib-protege41
cp -R target/site $ONTOP_REPORT_PATH/obdalib-protege41
