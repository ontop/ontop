#!/bin/sh

#
# ONTOP ENTRYPOINT SCRIPT
#
# The script runs the Ontop CLI main class with the supplied command line arguments, prepending
# extra arguments and configuring JVM arguments based on the value of several environment variables
# (see README.md for documentation about supported variables).
#

[ "${ONTOP_LOG_ENTRYPOINT-false}" != "false" ] && set -x -v

if [ $# -eq 0 ] || [ "${1#-}" != "$1" ]; then
  set -- "endpoint" "$@"
elif [ "$1" = "ontop" ]; then
  shift
else
  exec "$@"
fi

ONTOP_HOME=/opt/ontop

rm -f /tmp/.healthcheck  # reset status of healtcheck.sh (for 'query' healtcheck strategy)

assign_from_java_args_if_undef() {
  var=${1}; shift
  for arg in "$@"; do
    argval=${ONTOP_JAVA_ARGS##*-D${arg}=}
    if [ "${argval}" != "${ONTOP_JAVA_ARGS}" ]; then
      argval="${argval%% -*}"
      eval "varval=\"\$${var}\""
      if [ ! "${varval}" ]; then
        eval "${var}=\"${argval}\""
        choice="-D${arg}"
      elif [ "${varval}" != "${argval}" ]; then
        echo "WARNING: ${choice:-environment variable ${var}}=\"${varval}\" overrides -D${arg}=\"${argval}\" in ONTOP_JAVA_ARGS"
      fi
    fi
  done
}

if [ "${MAPPING_FILE+x}" ]; then
  ONTOP_MAPPING_FILE=${MAPPING_FILE}
  echo "WARNING: environment variable MAPPING_FILE is deprecated. Please use ONTOP_MAPPING_FILE instead"
fi

if [ "${ONTOP_MAPPING_FILE+x}" ]; then
  set -- "$@" "--mapping=${ONTOP_MAPPING_FILE}"
elif [ "$1" = "endpoint" ]; then
  echo "ERROR: environment variable ONTOP_MAPPING_FILE is not set" && exit 1
fi

if [ "${ONTOLOGY_FILE+x}" ]; then
  ONTOP_ONTOLOGY_FILE=${ONTOLOGY_FILE}
  echo "WARNING: environment variable ONTOLOGY_FILE is deprecated. Please use ONTOP_ONTOLOGY_FILE instead"
fi

if [ "${ONTOP_ONTOLOGY_FILE+x}" ]; then
  set -- "$@" "--ontology=${ONTOP_ONTOLOGY_FILE}"
fi

if [ "${PROPERTIES_FILE+x}" ]; then
  ONTOP_PROPERTIES_FILE=${PROPERTIES_FILE}
  echo "WARNING: environment variable PROPERTIES_FILE is deprecated. Please use ONTOP_PROPERTIES_FILE instead"
fi

if [ "${ONTOP_PROPERTIES_FILE+x}" ]; then
  set -- "$@" "--properties=${ONTOP_PROPERTIES_FILE}"
fi

if [ "${ONTOP_DB_USER+x}" ]; then
  set -- "$@" "--db-user=${ONTOP_DB_USER}"
fi

if [ "${ONTOP_DB_USER_FILE+x}" ]; then
  if [ "${ONTOP_DB_USER+x}" ]; then
    echo "ERROR: environment variables ONTOP_DB_USER and ONTOP_DB_USER_FILE are conflicting. Please choose one of the two." && exit 1
  fi
  set -- "$@" "--db-user=$(cat "${ONTOP_DB_USER_FILE}")"
fi

if [ "${ONTOP_DB_PASSWORD+x}" ]; then
  set -- "$@" "--db-password=${ONTOP_DB_PASSWORD}"
fi

if [ "${ONTOP_DB_PASSWORD_FILE+x}" ]; then
  if [ "${ONTOP_DB_PASSWORD+x}" ]; then
    echo "ERROR: environment variables ONTOP_DB_PASSWORD and ONTOP_DB_PASSWORD_FILE are conflicting. Please choose one of the two." && exit 1
  fi
  set -- "$@" "--db-password=$(cat "${ONTOP_DB_PASSWORD_FILE}")"
fi

if [ "${ONTOP_DB_URL+x}" ]; then
  set -- "$@" "--db-url=${ONTOP_DB_URL}"
fi

if [ "${ONTOP_DB_URL_FILE+x}" ]; then
  if [ "${ONTOP_DB_URL+x}" ]; then
    echo "ERROR: environment variables ONTOP_DB_URL and ONTOP_DB_URL_FILE are conflicting. Please choose one of the two." && exit 1
  fi
  set -- "$@" "--db-url=$(cat "${ONTOP_DB_URL_FILE}")"
fi

if [ "${ONTOP_DB_DRIVER+x}" ]; then
  set -- "$@" "--db-driver=${ONTOP_DB_DRIVER}"
fi

if [ "${ONTOP_XML_CATALOG_FILE+x}" ]; then
  set -- "$@" "--xml-catalog=${ONTOP_XML_CATALOG_FILE}"
fi

if [ "${ONTOP_CONSTRAINT_FILE+x}" ]; then
  set -- "$@" "--constraint=${ONTOP_CONSTRAINT_FILE}"
fi

if [ "${ONTOP_DB_METADATA_FILE+x}" ]; then
  set -- "$@" "--db-metadata=${ONTOP_DB_METADATA_FILE}"
fi

if [ "${ONTOP_VIEW_FILE+x}" ]; then
  echo "WARNING: environment variable ONTOP_VIEW_FILE is deprecated. Please use ONTOP_LENSES_FILE instead"
  ONTOP_LENSES_FILE=${ONTOP_VIEW_FILE}
fi

if [ "${ONTOP_LENSES_FILE+x}" ]; then
  set -- "$@" "--lenses=${ONTOP_LENSES_FILE}"
fi

if [ "${ONTOP_FACTS_FILE+x}" ]; then
  set -- "$@" "--facts=${ONTOP_FACTS_FILE}"
fi

if [ "${ONTOP_FACTS_FORMAT+x}" ]; then
  set -- "$@" "--facts-format=${ONTOP_FACTS_FORMAT}"
fi

if [ "${ONTOP_FACTS_BASE_IRI+x}" ]; then
  set -- "$@" "--facts-base-iri=${ONTOP_FACTS_BASE_IRI}"
fi


if [ "${ONTOP_SPARQL_RULES_FILE+x}" ]; then
  set -- "$@" "--sparql-rules=${ONTOP_SPARQL_RULES_FILE}"
fi

if [ "${ONTOP_CORS_ALLOWED_ORIGINS+x}" ]; then
  set -- "$@" "--cors-allowed-origins=${ONTOP_CORS_ALLOWED_ORIGINS}"
fi

if [ "${ONTOP_PORTAL_FILE+x}" ]; then
  set -- "$@" "--portal=${ONTOP_PORTAL_FILE}"
fi

if [ "${ONTOP_DISABLE_PORTAL_PAGE-false}" != "false" ]; then
  set -- "$@" "--disable-portal-page"
fi

if [ "${ONTOP_PREDEFINED_CONFIG+x}" ]; then
  set -- "$@" "--predefined-config=${ONTOP_PREDEFINED_CONFIG}"
fi

if [ "${ONTOP_PREDEFINED_QUERIES+x}" ]; then
  set -- "$@" "--predefined-queries=${ONTOP_PREDEFINED_QUERIES}"
fi

if [ "${ONTOP_CONTEXTS+x}" ]; then
  set -- "$@" "--contexts=${ONTOP_CONTEXTS}"
fi

if [ "${ONTOP_DEV_MODE-false}" != "false" ]; then
  set -- "$@" "--dev"
fi

if [ "${ONTOP_LAZY_INIT-false}" != "false" ]; then
  set -- "$@" "--lazy"
fi

if [ "${ONTOP_ENABLE_ANNOTATIONS-false}" != "false" ]; then
  set -- "$@" "--enable-annotations"
fi

if [ "${ONTOP_ENABLE_DOWNLOAD_ONTOLOGY-false}" != "false" ]; then
  set -- "$@" "--enable-download-ontology"
fi

if [ "${ONTOP_JAVA_ARGS#*-Xmx}" = "${ONTOP_JAVA_ARGS}" ]; then
  ONTOP_JAVA_ARGS="${ONTOP_JAVA_ARGS} -Xmx512m"
fi

assign_from_java_args_if_undef ONTOP_FILE_ENCODING "file.encoding"
: ${ONTOP_FILE_ENCODING:=UTF-8}

assign_from_java_args_if_undef ONTOP_LOG_CONFIG "logging.config" "logback.configurationFile"
: ${ONTOP_LOG_CONFIG:=${ONTOP_HOME}/log/logback.xml}

if [ -z "${ONTOP_LOG_LEVEL}" ]; then
  [ "${ONTOP_DEBUG-false}" != "false" ] && export ONTOP_LOG_LEVEL="debug" || export ONTOP_LOG_LEVEL="info"
elif [ ${ONTOP_DEBUG+x} ]; then
  echo "WARNING: environment variable ONTOP_DEBUG ignored due to ONTOP_LOG_LEVEL being specified"
fi

if [ "${ONTOP_CONFIGURE_JMX-false}" != "false" ]; then
  HOSTNAME=$( hostname -i | cut -d" " -f1 )
  JMX="-Dcom.sun.management.jmxremote"
  JMX="${JMX} -Dcom.sun.management.jmxremote.ssl=false"
  JMX="${JMX} -Dcom.sun.management.jmxremote.authenticate=false"
  JMX="${JMX} -Dcom.sun.management.jmxremote.local.only=false"
  JMX="${JMX} -Dcom.sun.management.jmxremote.port=8686"
  JMX="${JMX} -Dcom.sun.management.jmxremote.rmi.port=8686"
  JMX="${JMX} -Djava.rmi.server.hostname=${HOSTNAME}"
  ONTOP_JAVA_ARGS="${JMX} ${ONTOP_JAVA_ARGS}"
fi

if [ "${ONTOP_WAIT_FOR}" ]; then
  echo "INFO: waiting for availability of TCP ports ${ONTOP_WAIT_FOR}${ONTOP_WAIT_TIMEOUT+" (max ${ONTOP_WAIT_TIMEOUT}s)"}"
  TS=$(date +%s)
  for HOST_PORT in ${ONTOP_WAIT_FOR}; do
    HOST=${HOST_PORT%%:*}
    PORT=${HOST_PORT#*:}
    while true; do
      ELAPSED=$(( $(date +%s) - TS ))
      if nc -w1 -z ${HOST} ${PORT}; then
        echo "INFO: port ${HOST_PORT} available after ${ELAPSED}s"
        break
      elif [ "${ONTOP_WAIT_TIMEOUT}" ] && [ ${ELAPSED} -gt ${ONTOP_WAIT_TIMEOUT} ]; then
        echo "ERROR: port ${HOST_PORT} not available within required ${ONTOP_WAIT_TIMEOUT}s"
        exit 1
      else
        sleep 1
      fi
    done
  done
fi

exec java \
  ${ONTOP_JAVA_ARGS} \
  "-Dfile.encoding=${ONTOP_FILE_ENCODING}" \
  "-Dlogging.config=${ONTOP_LOG_CONFIG}" \
  "-Dlogback.configurationFile=${ONTOP_LOG_CONFIG}" \
  -cp "${ONTOP_HOME}/lib/*:${ONTOP_HOME}/jdbc/*" \
  it.unibz.inf.ontop.cli.Ontop "$@"
