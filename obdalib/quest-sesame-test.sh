#!/bin/bash

# Usage: ./quest-sesame=test.sh 

cd $ONTOP_BUILD_PATH/obdalib/quest-sesame
mvn clean
mvn site
rm -rf $ONTOP_REPORT_PATH/quest-sesame
cp -R target/site $ONTOP_REPORT_PATH/quest-sesame
