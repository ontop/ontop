#!/bin/sh

export JDBC_PLUGINS_PATH=/Applications/Protege_4.1-obdalib-1.7-alpha/plugins
export REVISION=2039

# Packing the -ontopPro- distribution
echo "pluginVersion=1.7-alpha.b$REVISION" >  obdalib-core/src/main/resources/it/unibz/krdb/obda/utils/version.properties
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
cd ..
cd ..
cat obdalib-core/src/main/resources/it/unibz/krdb/obda/utils/version.properties

# Packing the sesame distribution
cd quest-distribution/
mkdir -p dist/WEB-INF/lib
mvn assembly:assembly -DskipTests
cp target/quest-distribution-1.7-alpha-sesame-bin.jar dist/WEB-INF/lib
cp target/quest-distribution-1.7-alpha-dependencies.zip dist/
cp src/main/resources/openrdf/openrdf-sesame.war dist/
cp src/main/resources/openrdf/openrdf-workbench.war dist/
cp src/main/resources/jetty/jetty-distribution-8.1.9.zip dist/

cd dist
unzip quest-distribution-1.7-alpha-dependencies.zip -d WEB-INF/lib/
jar -uf openrdf-sesame.war WEB-INF/lib/*
jar -uf openrdf-workbench.war WEB-INF/lib/*

mkdir -p jetty-distribution-8.1.9/webapps
cp openrdf-sesame.war jetty-distribution-8.1.9/webapps
cp openrdf-workbench.war jetty-distribution-8.1.9/webapps

zip jetty-distribution-8.1.9.zip jetty-distribution-8.1.9/webapps/*

rm -fr jetty-distribution-8.1.9
rm -fr WEB-INF
rm -f quest-distribution-1.7-alpha-dependencies.zip
