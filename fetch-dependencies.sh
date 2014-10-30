#!/bin/sh
export ONTOP_DEP_HOME=/build/dependencies
export SESAME_VERSION=2.7.13
export SESAME_PREFIX=openrdf-sesame-${SESAME_VERSION}
export SESAME_SDK_FILE_PREFIX=${SESAME_PREFIX}-sdk
export SESAME_SDK_FILE=${SESAME_SDK_FILE_PREFIX}.tar.gz
export SESAME_WAR_FILE=openrdf-sesame.war
export SESAME_WORKBENCH_WAR_FILE=openrdf-workbench.war

cd $ONTOP_DEP_HOME
wget http://downloads.sourceforge.net/project/sesame/Sesame%202/${SESAME_VERSION}/${SESAME_SDK_FILE}
tar -xzvf $SESAME_SDK_FILE
mv $SESAME_PREFIX/war/${SESAME_WAR_FILE} .
mv $SESAME_PREFIX/war/${SESAME_WORKBENCH_WAR_FILE} .