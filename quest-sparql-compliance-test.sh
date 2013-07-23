#!/bin/bash

# Usage: ./quest-sparql-compliance-test.sh

cd $ONTOP_BUILD_PATH/obdalib/quest-sparql-compliance
mvn clean
mvn site
rm -rf $ONTOP_REPORT_PATH/quest-sparql-compliance
cp -R target/site $ONTOP_REPORT_PATH/quest-sparql-compliance
