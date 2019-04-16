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
elif [[ -n "${JAVA_HOME}" ]] && [[ -x "${JAVA_HOME}/bin/java" ]]; then
    JAVA="${JAVA_HOME}/bin/java"
else
    echo "ERROR: Java is not installed!"
    exit 1
fi

echo '$ java -version'

${JAVA} -version || exit 1

echo ""

JAVA_VER=$(${JAVA} -version 2>&1 | sed 's/version "\(.*\)\.\(.*\)\..*"/\2/; 1q')
#echo version "$version"
#if [[ "$JAVA_VER" != "java 8" ]]; then
#    echo "ERROR: Java 8 is required for building Ontop! Current Java version: $JAVA_VER"
#    exit 1
#fi

echo '$ ./mvnw -version'
./mvnw -version
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
ONTOP_DEP_HOME=${BUILD_ROOT}/build/dependencies


if [ -d "${ONTOP_DEP_HOME}" ] && [ -f "${ONTOP_DEP_HOME}/.git" ]
then
  echo ""
  echo "========================================="
  echo " Starting Ontop build script ... "
  echo "-----------------------------------------"
  echo ""
else
  echo "ERROR: git submodule 'ontop-build/dependencies' is missing or uninitiated!"
  echo "Please run 'git submodule init && git submodule update'"
  exit 1
fi

# location for protege clean folder
PROTEGE_COPY_FILENAME=Protege-5.5.0-platform-independent
PROTEGE_MAIN_FOLDER_NAME=Protege-5.5.0


# location and name for jetty distribution (should be ZIP)
JETTY_COPY_FILENAME=jetty-distribution-9.4.6
JETTY_INNER_FOLDERNAME=jetty-distribution-9.4.6.v20170531

# location and name for tomcat distribution (should be zip)
TOMCAT_FILENAME=apache-tomcat-8.5.9

# folder names of the output
PROTEGE_DIST=ontop-protege
ONTOP_JETTY_DIST=ontop-jetty
ONTOP_TOMCAT_DIST=ontop-tomcat
ONTOP_CLI=ontop-cli

# jar name of the protege plugin
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

mvn clean -q

echo ""
echo "========================================="
echo " Compiling                               "
echo "-----------------------------------------"
echo ""


mvn install -DskipTests -q || exit 1

echo "[INFO] Compilation completed"

VERSION=$(cat ${BUILD_ROOT}/engine/system/core/target/classes/version.properties | sed 's/version=\(.*\)/\1/')

#
echo ""
echo "========================================="
echo " Building Protege distribution package   "
echo "-----------------------------------------"
echo ""

cd ${BUILD_ROOT}/client/protege/
mvn bundle:bundle -DskipTests  || exit 1

rm -fr ${BUILD_ROOT}/build/distribution/${PROTEGE_DIST}
mkdir -p ${BUILD_ROOT}/build/distribution/${PROTEGE_DIST}
cp target/${PROTEGE_PLUGIN_NAME}-${VERSION}.jar \
  ${BUILD_ROOT}/build/distribution/${PROTEGE_DIST}/${PROTEGE_PLUGIN_NAME}-${VERSION}.jar

cp ${ONTOP_DEP_HOME}/${PROTEGE_COPY_FILENAME}.zip ${BUILD_ROOT}/build/distribution/${PROTEGE_DIST}/  || exit 1

cd ${BUILD_ROOT}/build/distribution/${PROTEGE_DIST}/

mkdir -p ${PROTEGE_MAIN_FOLDER_NAME}/plugins
cp ${PROTEGE_PLUGIN_NAME}-${VERSION}.jar ${PROTEGE_MAIN_FOLDER_NAME}/plugins/
zip ${PROTEGE_COPY_FILENAME}.zip ${PROTEGE_MAIN_FOLDER_NAME}/plugins/*
mv ${PROTEGE_COPY_FILENAME}.zip ontop-protege-bundle-${VERSION}.zip

rm -fr ${PROTEGE_MAIN_FOLDER_NAME}
cd ${BUILD_ROOT}/build/distribution

# Packing the rdf4j distribution
#
echo ""
echo "========================================="
echo " Building RDF4J distribution package    "
echo "-----------------------------------------"
echo ""

mkdir -p ${BUILD_ROOT}/build/distribution/ontop-webapps

cp ${BUILD_ROOT}/client/rdf4j-webapps/server/target/rdf4j-server.war ${BUILD_ROOT}/build/distribution/ontop-webapps
cp ${BUILD_ROOT}/client/rdf4j-webapps/workbench/target/rdf4j-workbench.war ${BUILD_ROOT}/build/distribution/ontop-webapps

cd ${BUILD_ROOT}/build/distribution/ontop-webapps
zip -r ontop-webapps-${VERSION}.zip *.war
cd ${BUILD_ROOT}/build/distribution

# Packaging the rdf4j jetty distribution
#
echo ""
echo "========================================="
echo " Building  Jetty distribution package    "
echo "-----------------------------------------"
echo ""

rm -fr ${ONTOP_JETTY_DIST}
mkdir -p ${ONTOP_JETTY_DIST}
cp ${ONTOP_DEP_HOME}/${JETTY_COPY_FILENAME}.zip ${ONTOP_JETTY_DIST}/ontop-jetty-bundle-${VERSION}.zip || exit 1

JETTY_FOLDER=${JETTY_INNER_FOLDERNAME}
cd ${ONTOP_JETTY_DIST}
mkdir -p ${JETTY_INNER_FOLDERNAME}/ontop-base/webapps
cp ${BUILD_ROOT}/build/distribution/ontop-webapps/*.war ${JETTY_FOLDER}/ontop-base/webapps
cp ${ONTOP_DEP_HOME}/start.ini ${JETTY_FOLDER}/ontop-base
cp ${ONTOP_DEP_HOME}/README-ontop.TXT ${JETTY_FOLDER}

zip -rq ontop-jetty-bundle-${VERSION}.zip ${JETTY_FOLDER}/ || exit 1

echo "[INFO] Built ontop-jetty-bundle-${VERSION}.zip"

rm -fr ${JETTY_FOLDER}
cd ${BUILD_ROOT}/build/distribution

# Packaging the tomcat distribution
#
echo ""
echo "========================================="
echo " Building Tomcat distribution package    "
echo "-----------------------------------------"
echo ""

rm -fr ${ONTOP_TOMCAT_DIST}
mkdir -p ${ONTOP_TOMCAT_DIST}
cp ${ONTOP_DEP_HOME}/${TOMCAT_FILENAME}.zip ${ONTOP_TOMCAT_DIST}/ontop-tomcat-bundle-${VERSION}.zip || exit 1

cd ${ONTOP_TOMCAT_DIST}
mkdir -p ${TOMCAT_FILENAME}/webapps
cp ${BUILD_ROOT}/build/distribution/ontop-webapps/*.war ${TOMCAT_FILENAME}/webapps

zip ontop-tomcat-bundle-${VERSION}.zip ${TOMCAT_FILENAME}/webapps/* || exit 1

echo "[INFO] Built ontop-tomcat-bundle-${VERSION}.zip"

rm -fr ${TOMCAT_FILENAME}
cd ${BUILD_ROOT}/build/distribution

# Packaging the cli distribution
#
echo ""
echo "========================================="
echo " Building Ontop CLI distribution package     "
echo "-----------------------------------------"
echo ""

mvn assembly:assembly
rm -fr ${ONTOP_CLI}
mkdir -p ${ONTOP_CLI}
echo "[INFO] Copying files..."
cp target/ontop-distribution-${VERSION}.zip ${ONTOP_CLI}

echo ""
echo "========================================="
echo " Done.                                   "
echo "-----------------------------------------"
echo ""
