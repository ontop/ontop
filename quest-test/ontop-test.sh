#!/bin/bash

export ONTOP_REPORT_PATH=/var/lib/tomcat7/webapps/ontop-test-result
mvn clean
mvn site -DskipTests=false
rm -rf ${ONTOP_REPORT_PATH}/ontop-test
cp -R target/site ${ONTOP_REPORT_PATH}/ontop-test
