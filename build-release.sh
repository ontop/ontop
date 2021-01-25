d #!/usr/bin/env bash

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
#   - wget
#
########################################################################

# user could invoke the script as 'sh build-release.sh'
if [ ! -n "$BASH" ]; then
    echo "Please run this script with bash or run it as ./$0"
    exit 1
fi

# location for the build ROOT folder (i.e. the directory of this script)
BUILD_ROOT="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"

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
$BUILD_ROOT/mvnw -version
echo ""

echo "$ git --version"
git --version || exit 1
echo ""


# location for the build dependencies home
ONTOP_DEP_HOME=${BUILD_ROOT}/build/dependencies

echo ""
echo "========================================="
echo " Starting Ontop build script ... "
echo "-----------------------------------------"
echo ""

# location for protege clean folder
PROTEGE_URL="https://github.com/protegeproject/protege-distribution/releases/download/v5.5.0/Protege-5.5.0-platform-independent.zip"
PROTEGE_COPY_FILENAME=Protege-5.5.0-platform-independent
PROTEGE_MAIN_FOLDER_NAME=Protege-5.5.0
if [ ! -f ${ONTOP_DEP_HOME}/${PROTEGE_COPY_FILENAME}.zip ] ; then
  wget ${PROTEGE_URL} -P ${ONTOP_DEP_HOME} || exit 1
fi

# location and name for jetty distribution (should be ZIP)
# location for protege clean folder
JETTY_URL="https://repo1.maven.org/maven2/org/eclipse/jetty/jetty-distribution/9.4.20.v20190813/jetty-distribution-9.4.20.v20190813.zip"
JETTY_COPY_FILENAME=jetty-distribution-9.4.20.v20190813
JETTY_INNER_FOLDERNAME=jetty-distribution-9.4.20.v20190813
if [ ! -f ${ONTOP_DEP_HOME}/${JETTY_COPY_FILENAME}.zip ] ; then
  wget ${JETTY_URL} -P ${ONTOP_DEP_HOME} ||  exit 1
fi

# location and name for tomcat distribution (should be zip)
TOMCAT_URL="https://archive.apache.org/dist/tomcat/tomcat-9/v9.0.24/bin/apache-tomcat-9.0.24.zip"
TOMCAT_FILENAME=apache-tomcat-9.0.24
if [ ! -f ${ONTOP_DEP_HOME}/${TOMCAT_FILENAME}.zip ] ; then
  wget ${TOMCAT_URL} -P ${ONTOP_DEP_HOME} || exit 1
fi

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

$BUILD_ROOT/mvnw clean -q

echo ""
echo "========================================="
echo " Compiling                               "
echo "-----------------------------------------"
echo ""


$BUILD_ROOT/mvnw install -DskipTests || exit 1

echo "[INFO] Compilation completed"

VERSION=$(cat ${BUILD_ROOT}/engine/system/core/target/classes/version.properties | sed 's/version=\(.*\)/\1/')

#
echo ""
echo "========================================="
echo " Building Protege distribution package   "
echo "-----------------------------------------"
echo ""

cd ${BUILD_ROOT}/client/protege/
$BUILD_ROOT/mvnw bundle:bundle -DskipTests  || exit 1

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
mkdir -p ${JETTY_INNER_FOLDERNAME}/ontop-base/lib/ext
cp ${BUILD_ROOT}/build/distribution/ontop-webapps/*.war ${JETTY_FOLDER}/ontop-base/webapps
cp ${BUILD_ROOT}/client/jetty/start.ini ${JETTY_FOLDER}/ontop-base
cp ${BUILD_ROOT}/client/jetty/README.md ${JETTY_FOLDER}/README_ONTOP_JETTY.md

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

$BUILD_ROOT/mvnw assembly:single
rm -fr ${ONTOP_CLI}
mkdir -p ${ONTOP_CLI}
echo "[INFO] Copying files..."
cp target/${ONTOP_CLI}-${VERSION}.zip ${ONTOP_CLI}

echo ""
echo "========================================="
echo " Done.                                   "
echo "-----------------------------------------"
echo ""
