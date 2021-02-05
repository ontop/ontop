#!/bin/sh

ONTOP_HOME=/opt/ontop

if [ -z "${ONTOP_JAVA_ARGS}" ]; then
  ONTOP_JAVA_ARGS="-Xmx512m"
fi

java ${ONTOP_JAVA_ARGS} -cp "${ONTOP_HOME}/lib/*:${ONTOP_HOME}/jdbc/*" -Dlogging.config="${ONTOP_HOME}/log/logback.xml" \
 it.unibz.inf.ontop.cli.Ontop $@
