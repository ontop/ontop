#!/bin/bash

# Usage: ./quest-rdb2rdf-compliance-test.sh

cd $ONTOP_BUILD_PATH/obdalib/quest-rdb2rdf-compliance
mvn clean
mvn site
rm -rf $ONTOP_REPORT_PATH/quest-rdb2rdf-compliance
cp -R target/site $ONTOP_REPORT_PATH/quest-rdb2rdf-compliance
