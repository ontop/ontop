#!/bin/bash

# Usage: ./reformulation-core-test.sh 

cd $ONTOP_BUILD_PATH/obdalib/reformulation-core
mvn clean
mvn site
rm -rf $ONTOP_REPORT_PATH/reformulation-core
cp -R target/site $ONTOP_REPORT_PATH/reformulation-core
