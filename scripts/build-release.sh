#!/bin/sh

###
# README:
#
# The required components for building the release can be found: http://obda.inf.unibz.it/files/dev/Dependencies.zip
# 
# - In UNIX system, locate the dependencies folder in ONTOP_DEP_HOME variable.
#
###

# location for the build dependencies home
export ONTOP_DEP_HOME=/build/dependencies

# location for the JDBC plugin jars
export JDBC_PLUGINS_PATH=$ONTOP_DEP_HOME

# location for protege clean folder (Protege 4.2)
export PROTEGE_COPY_PATH=$ONTOP_DEP_HOME
export PROTEGE_COPY_FILENAME=protege-4.2-beta.284
export PROTEGE_MAIN_FOLDER_NAME=Protege_4.2
export PROTEGE_MAIN_PLUGIN=ontopro-plugin

# location and name for jetty distribution (should be ZIP)
export JETTY_COPY_PATH=$ONTOP_DEP_HOME
export JETTY_COPY_FILENAME=jetty-distribution-8.1.9
export JETTY_INNER_FOLDERNAME=jetty-distribution-8.1.9

#location for sesame and workbench WEB-APP jars
export OPENRDF_WORKBENCH_PATH=$ONTOP_DEP_HOME
export OPENRDF_SESAME_PATH=$ONTOP_DEP_HOME      

# name of the wars for sesame and workbench WEB-APPs  (these have to be already customized with stylesheets)
export OPENRDF_SESAME_FILENAME=openrdf-sesame
export OPENRDF_WORKBENCH_FILENAME=openrdf-workbench
                
# folder names of the output
export PROTEGE_DIST=ontopPro
export QUEST_SESAME_DIST=QuestSesame
export QUEST_JETTY_DIST=QuestJetty
export OWL_API_DIST=QuestOWL

export VERSION=1.8
export REVISION=0

#svn update
mvn clean

# Packing the -ontopPro- distribution
#
echo ""
echo "========================================="
echo " Making -ontopPro- distribution package"
echo "-----------------------------------------"
echo "pluginVersion=$VERSION.$REVISION" >  obdalib-core/src/main/resources/it/unibz/krdb/obda/utils/version.properties
rm -fr obdalib-protege41/dist
mvn install -DskipTests
cd obdalib-protege41/
mvn bundle:bundle -DskipTests

rm -fr ../quest-distribution/$PROTEGE_DIST
mkdir ../quest-distribution/$PROTEGE_DIST
cp target/it.unibz.inf.obda.p4plugin-$VERSION.jar ../quest-distribution/$PROTEGE_DIST/it.unibz.inf.obda.p4plugin-$VERSION.$REVISION.jar
cp $PROTEGE_COPY_PATH/$PROTEGE_COPY_FILENAME.zip ../quest-distribution/$PROTEGE_DIST/

cd ../quest-distribution/$PROTEGE_DIST/

mkdir -p $PROTEGE_MAIN_FOLDER_NAME/plugins
cp it.unibz.inf.obda.p4plugin-$VERSION.$REVISION.jar $PROTEGE_MAIN_FOLDER_NAME/plugins/
cp $JDBC_PLUGINS_PATH/org.protege.osgi.jdbc.jar $PROTEGE_MAIN_FOLDER_NAME/plugins/
cp $JDBC_PLUGINS_PATH/org.protege.osgi.jdbc.prefs.jar $PROTEGE_MAIN_FOLDER_NAME/plugins/
zip ../$PROTEGE_DIST/$PROTEGE_MAIN_PLUGIN-$VERSION.$REVISION.zip $PROTEGE_MAIN_FOLDER_NAME/plugins/*.*

zip $PROTEGE_COPY_FILENAME.zip $PROTEGE_MAIN_FOLDER_NAME/plugins/*
mv $PROTEGE_COPY_FILENAME.zip $PROTEGE_MAIN_PLUGIN-with-protege-$VERSION.$REVISION.zip

rm -fr $PROTEGE_MAIN_FOLDER_NAME
cd ..

# Packing the sesame distribution
#
echo ""
echo "========================================="
echo " Making Sesame distribution package"
echo "-----------------------------------------"
rm -fr $QUEST_SESAME_DIST
mkdir -p $QUEST_SESAME_DIST/WEB-INF/lib
mvn assembly:assembly -DskipTests
cp target/quest-distribution-$VERSION-sesame-bin.jar $QUEST_SESAME_DIST/WEB-INF/lib/ontop-distribution-$VERSION.$REVISION.jar
unzip -q -d $QUEST_SESAME_DIST/WEB-INF/lib/ target/quest-distribution-$VERSION-dependencies.zip
cp $OPENRDF_SESAME_PATH/$OPENRDF_SESAME_FILENAME.war $QUEST_SESAME_DIST/
cp $OPENRDF_WORKBENCH_PATH/$OPENRDF_WORKBENCH_FILENAME.war $QUEST_SESAME_DIST/

cd $QUEST_SESAME_DIST
echo ""
echo "[INFO] Adding QuestSesame and dependency JARs to openrdf-sesame.war"
jar -uf $OPENRDF_SESAME_FILENAME.war WEB-INF/lib/*

echo "[INFO] Adding QuestSesame and dependency JARs to openrdf-workbench.war"
jar -uf $OPENRDF_WORKBENCH_FILENAME.war WEB-INF/lib/*

rm -fr WEB-INF
cd ..

# Packaging the sesame jetty distribution
#
echo ""
echo "========================================="
echo " Making Sesame Jetty distribution package"
echo "-----------------------------------------"
rm -fr $QUEST_JETTY_DIST
mkdir $QUEST_JETTY_DIST
cp $JETTY_COPY_PATH/$JETTY_COPY_FILENAME.zip $QUEST_JETTY_DIST/ontop-with-jetty-$VERSION.$REVISION.zip

export JETTY_FOLDER=$JETTY_INNER_FOLDERNAME
cd $QUEST_JETTY_DIST
mkdir -p $JETTY_INNER_FOLDERNAME/webapps
cp ../$QUEST_SESAME_DIST/$OPENRDF_SESAME_FILENAME.war $JETTY_FOLDER/webapps
cp ../$QUEST_SESAME_DIST/$OPENRDF_WORKBENCH_FILENAME.war $JETTY_FOLDER/webapps

zip ontop-with-jetty-$VERSION.$REVISION.zip $JETTY_FOLDER/webapps/*

rm -fr $JETTY_FOLDER
cd ..

# Packaging the OWL-API distribution
#
echo ""
echo "========================================="
echo " Making OWL-API distribution package"
echo "-----------------------------------------"
rm -fr $OWL_API_DIST
mkdir $OWL_API_DIST
echo "[INFO] Copying files..."
cp target/quest-distribution-$VERSION-bin.zip $OWL_API_DIST/ontop-distribution-$VERSION.$REVISION.zip

echo ""
echo "Done."
echo ""
