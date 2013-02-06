#!/bin/sh

export JDBC_PLUGINS_PATH=/Applications/Protege_4.1-obdalib-1.7-alpha2/plugins
#export PROTEGE_COPY_PATH=
#export PROTEGE_COPY_FILENAME=
#export JETTY_COPY_PATH=
#export JETTY_COPY_FILENAME=jetty-distribution-8.1.9
#export OPENRDF_SESAME_PATH=
#export OPENRDF_SESAME_FILENAME=openrdf-sesame
#export OPENRDF_WORKBENCH_PATH=
#export OPENRDF_WORKBENCH_FILENAME=openrdf-workbench

export PROTEGE_DIST=ontopPro
export QUEST_SESAME_DIST=QuestSesame
export QUEST_JETTY_DIST=QuestJetty
export OWL_API_DIST=QuestOWL

export REVISION=2039

svn update
mvn clean

# Packing the -ontopPro- distribution
#
echo "pluginVersion=1.7-alpha2.b$REVISION" >  obdalib-core/src/main/resources/it/unibz/krdb/obda/utils/version.properties
rm -fr obdalib-protege41/dist
mvn install -DskipTests
cd obdalib-protege41/
mvn bundle:bundle -DskipTests

rm -fr ../quest-distribution/$PROTEGE_DIST
mkdir ../quest-distribution/$PROTEGE_DIST
cp target/it.unibz.inf.obda.p4plugin-1.7a1.jar ../quest-distribution/$PROTEGE_DIST/it.unibz.inf.obda.p4plugin-1.7-alpha2-b$REVISION.jar
cp $PROTEGE_COPY_PATH/$PROTEGE_COPY_FILENAME.zip ../quest-distribution/$PROTEGE_DIST/

cd ../quest-distribution/$PROTEGE_DIST/

cp it.unibz.inf.obda.p4plugin-1.7-alpha2-b$REVISION.jar $PROTEGE_COPY_FILENAME/plugins/
cp $JDBC_PLUGINS_PATH/org.protege.osgi.jdbc.jar $PROTEGE_COPY_FILENAME/plugins/
cp $JDBC_PLUGINS_PATH/org.protege.osgi.jdbc.prefs.jar $PROTEGE_COPY_FILENAME/plugins/

zip $PROTEGE_COPY_FILENAME.zip $PROTEGE_COPY_FILENAME/plugins/*

rm -fr $PROTEGE_COPY_FILENAME

ls -lah
cd ..

# Packing the sesame distribution
#
rm -fr $QUEST_SESAME_DIST
mkdir -p $QUEST_SESAME_DIST/WEB-INF/lib
mvn assembly:assembly -DskipTests
cp target/quest-distribution-1.7-alpha2-sesame-bin.jar $QUEST_SESAME_DIST/WEB-INF/lib/quest-distribution-1.7-alpha2-sesame-b$REVISION.jar
unzip target/quest-distribution-1.7-alpha2-dependencies.zip $QUEST_SESAME_DIST/WEB-INF/lib
cp $OPENRDF_SESAME_PATH/$OPENRDF_SESAME_FILENAME.war $QUEST_SESAME_DIST/
cp $OPENRDF_WORKBENCH_PATH/$OPENRDF_WORKBENCH_FILENAME.war $QUEST_SESAME_DIST/

cd $QUEST_SESAME_DIST
jar -uf $OPENRDF_SESAME_FILENAME.war WEB-INF/lib/*
jar -uf $OPENRDF_WORKBENCH_FILENAME.war WEB-INF/lib/*

rm -fr WEB-INF
cd ..

# Packaging the sesame jetty distribution
#
rm -fr $QUEST_JETTY_DIST
mkdir $QUEST_JETTY_DIST
cp $JETTY_COPY_PATH/$JETTY_COPY_FILENAME.zip $QUEST_SESAME_DIST/

$JETTY_FOLDER=$JETTY_COPY_FILENAME
cd $QUEST_JETTY_DIST
mkdir -p $JETTY_FOLDER/webapps
cp ../quest-distribution/$QUEST_SESAME_DIST/$OPENRDF_SESAME_FILENAME.war $JETTY_FOLDER/webapps
cp ../quest-distribution/$QUEST_SESAME_DIST/$OPENRDF_WORKBENCH_FILENAME.war $JETTY_FOLDER/webapps

zip $JETTY_COPY_FILENAME.zip $JETTY_FOLDER/webapps/*

rm -fr $JETTY_FOLDER
cd ..

# Packaging the OWL-API distribution
#
rm -fr $OWL_API_DIST
mkdir $OWL_API_DIST
cp target/quest-distribution-1.7-alpha2-bin.zip $OWL_API_DIST/quest-distribution-1.7-alpha2-b$REVISION.zip

echo "Done."