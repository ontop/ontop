#! /bin/bash

# Builds the Protege plugin for debug purposes
# Source: http://github.com/ontop/ontop/wiki/ObdalibPluginDebug

# Retrieve Ontop's version from the pom.xml file (assumption: can be found in the first line matching '<version>(.*)<\version>')
version=`grep -m 1 'version' < pom.xml | sed -r 's/.*<version>(.*)<\/version>.*/\1/' | tr -d '\n'`
pwd=`pwd`

# Build ontop bundles
cd "$pwd/../.."
mvn clean install -DskipTests

# Compile ontop-protege plugin jar file
cd $pwd
mvn bundle:bundle

# Copy the jar to Protege plugins.
cp "$pwd/target/it.unibz.inf.ontop.protege-$version.jar" "$pwd/../../build/dependencies/protege/Protege-5.5.0-beta-8/plugins"
