#!/bin/bash

# Usage: ./quest-owlapi3.sh

cd $ONTOP_BUILD_PATH/obdalib/quest-owlapi3
mvn site
rm -rf $ONTOP_REPORT_PATH/quest-owlapi3
cp -R target/site $ONTOP_REPORT_PATH/quest-owlapi3
