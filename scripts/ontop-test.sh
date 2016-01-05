#!/bin/bash

# Usage: ./quest-sesame=test.sh 
export BUILD_ROOT="$( cd "$( dirname "${BASH_SOURCE[0]}/" )" && cd .. && pwd )"
cd $BUILD_ROOT
mvn clean
mvn install -DskipTests=true
cd $BUILD_ROOT/../quest-sesame
mvn install -DskipTests=false
mvn site
rm -rf $ONTOP_REPORT_PATH/quest-test
cp -R target/site $ONTOP_REPORT_PATH/quest-test
