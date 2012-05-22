#!/bin/sh

export JDBC_PLUGINS_PATH=/Applications/Protege_4.1-obdalib-1.7-alpha/plugins
export REVISION=1479
rm -fr obdalib-protege41/dist
svn update
mvn clean
mvn install -DskipTests
cd obdalib-protege41/
mvn bundle:bundle -DskipTests
rm -fr dist
mkdir dist
cp target/it.unibz.inf.obda.p4plugin-1.7a1.jar dist/it.unibz.inf.obda.p4plugin-1.7-alpha-b$REVISION.jar
cp $JDBC_PLUGINS_PATH/org.protege.osgi.jdbc.jar dist/
cp $JDBC_PLUGINS_PATH/org.protege.osgi.jdbc.prefs.jar dist/
cd dist
zip it.unibz.inf.obda.p4plugin-1.7-alpha-b$REVISION.zip *.jar
ls -lah




