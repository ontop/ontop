#!/usr/bin/env bash

########################################################################
#
#                       Ontop build script
# 
#                      <xiao(a)inf.unibz.it>
#
#   Build Requirements
#   - Java 8
#   - Maven
#   - git 
#   - git-lfs
#
########################################################################


if type -p java; then
    JAVA=java
elif [[ -n "$JAVA_HOME" ]] && [[ -x "$JAVA_HOME/bin/java" ]]; then
    JAVA="$JAVA_HOME/bin/java"
else
    echo "ERROR: Java is not installed!"
    exit 1
fi

echo '$ java -version'

${JAVA} -version || exit 1

echo ""

JAVA_VER=$(${JAVA} -version 2>&1 | sed 's/version "\(.*\)\.\(.*\)\..*"/\2/; 1q')
#echo version "$version"
if [[ "$JAVA_VER" -ne "8" ]]; then
    echo "ERROR: Java 8 is required for building Ontop! Current Java version: $JAVA_VER"
    exit 1
fi

echo '$ mvn -version'
mvn -version || { echo "ERROR: maven is not installed!" ; exit 1 ; }
echo ""

echo "$ git --version"
git --version || exit 1
echo ""

echo "$ git lfs env"
git lfs env ||  { echo "ERROR: git-lfs is not installed or not configured!" ; exit 1 ; }
echo ""

# location for the build ROOT folder (i.e. the directory of this script)
BUILD_ROOT="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"

# location for the build dependencies home 
ONTOP_DEP_HOME=${BUILD_ROOT}/ontop-build-dependencies


if [ -d "${ONTOP_DEP_HOME}" ] && [ -f "${ONTOP_DEP_HOME}/.git" ]
then
  echo ""
  echo "========================================="
  echo " Starting Ontop build script ... "
  echo "-----------------------------------------"
  echo ""
else
  echo "ERROR: git submodule 'ontop-build-dependencies' is missing or uninitiated!"
  echo "Please run 'git submodule init && git submodule update'"
  exit 1
fi

# location for protege clean folder
PROTEGE_COPY_FILENAME=protege-5.0.0-beta-21-platform-independent
PROTEGE_MAIN_FOLDER_NAME=Protege-5.0.0-beta-21
PROTEGE_MAIN_PLUGIN=ontop-protege-plugin

# location and name for jetty distribution (should be ZIP)
JETTY_COPY_FILENAME=jetty-distribution-8.1.9
JETTY_INNER_FOLDERNAME=jetty-distribution-8.1.9

# name of the wars for sesame and workbench WEB-APPs  (these have to be already customized with stylesheets)
OPENRDF_SESAME_FILENAME=openrdf-sesame
OPENRDF_WORKBENCH_FILENAME=openrdf-workbench
ONTOP_SESAME_WEBAPPS=ontop-sesame-webapps

# folder names of the output
PROTEGE_DIST=ontop-protege
QUEST_SESAME_DIST=ontop-sesame
QUEST_JETTY_DIST=ontop-jetty
ONTOP_DIST=ontop-dist

# jar name of the pretege plugin
PROTEGE_PLUGIN_NAME=it.unibz.inf.ontop.protege

#
# Start building the packages
#

cd ${BUILD_ROOT}

echo ""
echo "========================================="
echo " Cleaning                                "
echo "-----------------------------------------"
echo ""

mvn clean

echo ""
echo "========================================="
echo " Compiling                               "
echo "-----------------------------------------"
echo ""


mvn install -DskipTests || exit 1

VERSION=$(cat ${BUILD_ROOT}/obdalib-core/target/classes/version.properties | sed 's/version=\(.*\)/\1/')

#
echo ""
echo "========================================="
echo " Building Protege distribution package   "
echo "-----------------------------------------"
echo ""

rm -fr ${BUILD_ROOT}/ontop-protege/dist
cd ${BUILD_ROOT}/ontop-protege/
mvn bundle:bundle -DskipTests  || exit 1

rm -fr ${BUILD_ROOT}/quest-distribution/${PROTEGE_DIST}
mkdir ${BUILD_ROOT}/quest-distribution/${PROTEGE_DIST}
cp target/${PROTEGE_PLUGIN_NAME}-${VERSION}.jar \
  ${BUILD_ROOT}/quest-distribution/${PROTEGE_DIST}/${PROTEGE_PLUGIN_NAME}-${VERSION}.jar

cp ${ONTOP_DEP_HOME}/${PROTEGE_COPY_FILENAME}.zip ${BUILD_ROOT}/quest-distribution/${PROTEGE_DIST}/  || exit 1

cd ${BUILD_ROOT}/quest-distribution/${PROTEGE_DIST}/

mkdir -p ${PROTEGE_MAIN_FOLDER_NAME}/plugins
cp ${PROTEGE_PLUGIN_NAME}-${VERSION}.jar ${PROTEGE_MAIN_FOLDER_NAME}/plugins/
zip ${PROTEGE_COPY_FILENAME}.zip ${PROTEGE_MAIN_FOLDER_NAME}/plugins/*
mv ${PROTEGE_COPY_FILENAME}.zip ontop-protege-bundle-${VERSION}.zip

rm -fr ${PROTEGE_MAIN_FOLDER_NAME}
cd ${BUILD_ROOT}/quest-distribution

# Packing the sesame distribution
#
echo ""
echo "========================================="
echo " Building Sesame distribution package    "
echo "-----------------------------------------"
echo ""

rm -fr ${QUEST_SESAME_DIST}
mkdir -p ${QUEST_SESAME_DIST}/WEB-INF/lib
mvn assembly:assembly -DskipTests  || exit 1
cp target/ontop-distribution-${VERSION}-sesame-bin.jar ${QUEST_SESAME_DIST}/WEB-INF/lib/ontop-distribution-${VERSION}.jar || exit 1
unzip -q -d ${QUEST_SESAME_DIST}/WEB-INF/lib/ target/ontop-distribution-${VERSION}-dependencies.zip || exit 1
cp ${ONTOP_DEP_HOME}/${OPENRDF_SESAME_FILENAME}.war ${QUEST_SESAME_DIST}/
cp ${ONTOP_DEP_HOME}/${OPENRDF_WORKBENCH_FILENAME}.war ${QUEST_SESAME_DIST}/

cd ${QUEST_SESAME_DIST}
echo ""
echo "[INFO] Adding QuestSesame and dependency JARs to openrdf-sesame.war"
jar -uf ${OPENRDF_SESAME_FILENAME}.war WEB-INF/lib/* || exit 1

echo "[INFO] Adding QuestSesame and dependency JARs to openrdf-workbench.war"
jar -uf ${OPENRDF_WORKBENCH_FILENAME}.war WEB-INF/lib/* || exit 1

zip ${ONTOP_SESAME_WEBAPPS}-${VERSION}.zip ${OPENRDF_SESAME_FILENAME}.war ${OPENRDF_WORKBENCH_FILENAME}.war || exit 1

rm -fr WEB-INF
cd ${BUILD_ROOT}/quest-distribution

# Packaging the sesame jetty distribution
#
echo ""
echo "========================================="
echo " Building  Jetty distribution package    "
echo "-----------------------------------------"
rm -fr ${QUEST_JETTY_DIST}
mkdir ${QUEST_JETTY_DIST}
cp ${ONTOP_DEP_HOME}/${JETTY_COPY_FILENAME}.zip ${QUEST_JETTY_DIST}/ontop-jetty-bundle-${VERSION}.zip || exit 1

JETTY_FOLDER=${JETTY_INNER_FOLDERNAME}
cd ${QUEST_JETTY_DIST}
mkdir -p ${JETTY_INNER_FOLDERNAME}/webapps
cp ${BUILD_ROOT}/quest-distribution/${QUEST_SESAME_DIST}/${OPENRDF_SESAME_FILENAME}.war ${JETTY_FOLDER}/webapps
cp ${BUILD_ROOT}/quest-distribution/${QUEST_SESAME_DIST}/${OPENRDF_WORKBENCH_FILENAME}.war ${JETTY_FOLDER}/webapps

zip ontop-jetty-bundle-${VERSION}.zip ${JETTY_FOLDER}/webapps/* || exit 1

rm -fr ${JETTY_FOLDER}
cd ${BUILD_ROOT}/quest-distribution

# Packaging the OWL-API distribution
#
echo ""
echo "========================================="
echo " Building Ontop distribution package     "
echo "-----------------------------------------"
rm -fr ${ONTOP_DIST}
mkdir ${ONTOP_DIST}
echo "[INFO] Copying files..."
cp target/ontop-distribution-${VERSION}-bin.zip ${ONTOP_DIST}/ontop-distribution-${VERSION}.zip

echo ""
echo "========================================="
echo " Done.                                   "
echo "-----------------------------------------"
echo ""
