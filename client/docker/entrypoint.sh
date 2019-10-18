#!/bin/bash

ONTOP_HOME=/opt/ontop

args_array=()

if [ "${ONTOP_MAPPING_FILE+x}" ]; then
  args_array+=("--mapping=${ONTOP_MAPPING_FILE}")
else
  echo "ERROR: environment varialbe ONTOP_MAPPING_FILE is not set" && exit 1
fi

if [ "${ONTOP_ONTOLOGY_FILE+x}" ]; then
  args_array+=("--ontology=${ONTOP_ONTOLOGY_FILE}")
fi

if [ "${ONTOP_PROPERTIES_FILE+x}" ]; then
  args_array+=("--properties=${ONTOP_PROPERTIES_FILE}")
else
  echo "ERROR: environment varialbe ONTOP_PROPERTIES_FILE is not set" && exit 1
fi

if [ "${ONTOP_CONSTRAINT_FILE+x}" ]; then
  args_array+=("--constratin=${ONTOP_CONSTRAINT_FILE}")
fi

if [ "${ONTOP_CORS_ALLOWED_ORIGINS+x}" ]; then
  args_array+=("--cors-allowed-origins=${ONTOP_CORS_ALLOWED_ORIGINS}")
fi

if [ "${ONTOP_DEV_MODE+x}" ]; then
  args_array+=("--dev")
fi

if [ "${ONTOP_LAZY_INIT+x}" ]; then
  args_array+=("--lazy")
fi

if [ -z "${ONTOP_JAVA_ARGS+x}" ]; then
  ONTOP_JAVA_ARGS="-Xmx512m"
fi

if [ -z "${ONTOP_DEBUG+x}" ]; then
  LOGBACK_CONFIG_FILE=${ONTOP_HOME}/log/logback-debug.xml
else
  LOGBACK_CONFIG_FILE=${ONTOP_HOME}/log/logback.xml
fi

java ${ONTOP_JAVA_ARGS} -cp "${ONTOP_HOME}/lib/*:${ONTOP_HOME}/jdbc/*" -Dlogback.configurationFile="${LOGBACK_CONFIG_FILE}" \
 it.unibz.inf.ontop.cli.Ontop endpoint "${args_array[@]}"
