#! /bin/bash

# Builds the Protege plugin for debug purposes
# Source: https://ontop-vkg.org/dev/debug-protege.html

#Protege version
pVersion="5.5.0"

# Retrieve Ontop's version from the pom.xml file (assumption: can be found in the first line matching '<version>(.*)<\version>')
version=`grep -m 1 'version' < pom.xml | sed -r 's/.*<version>(.*)<\/version>.*/\1/' | tr -d '\n'`
wd=`pwd`

# Build ontop
cd "${wd}/../.."
mvn clean install -DskipTests

# Compile ontop-protege plugin jar file
cd $wd
mvn bundle:bundle

# Copy the jar to Protege plugins
destDir=${wd}/../../build/distribution/ontop-protege/Protege-${pVersion}/plugins
mkdir -p $destDir
cp $wd/target/it.unibz.inf.ontop.protege-*.jar $destDir
