#!/bin/sh

#
# ONTOP ENTRYPOINT SCRIPT
#
# The script runs the Ontop CLI main class with the supplied command line arguments, prepending
# extra arguments and configuring JVM arguments based on the value of several environment variables
# (see README.md for documentation about supported variables).
#

# Echo each statemement being run as part of the script, if ONTOP_LOG_ENTRYPOINT is set
[ "${ONTOP_LOG_ENTRYPOINT-false}" != "false" ] && set -x -v

# Check argument list
if [ $# -eq 0 ] || [ "${1#-}" != "$1" ]; then
  set -- "endpoint" "$@"  # run 'ontop endpoint' with processed arg list, if there are no arguments or the start with an '-option'
elif [ "$1" = "ontop" ]; then
  shift  # run 'ontop' with processed arg list, if it starts with 'ontop' (which is then discarded)
else
  exec "$@"  # run whatever other command (e.g., bash) has been specified
fi

# Assume ontop is installed in /opt/ontop
ONTOP_HOME=/opt/ontop

# Reset status of healtcheck.sh (for 'query' healtcheck strategy)
rm -f /tmp/.healthcheck

#
# Helper function taking an environment to assign followed by one or more names of Java system properties.
# If any of these properties is present in the user-supplied value of ONTOP_JAVA_ARGS and the specified
# environment variable is unset, then the latter is set based on the value of the former.
#
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

# Inject -Xmx512m into ONTOP_JAVA_ARGS, in case it does not alreaady specify a value for -Xmx
if [ "${ONTOP_JAVA_ARGS#*-Xmx}" = "${ONTOP_JAVA_ARGS}" ]; then
  ONTOP_JAVA_ARGS="${ONTOP_JAVA_ARGS} -Xmx512m"
fi

# Assign ONTOP_FILE_ENCODING based on ONTOP_JAVA_ARGS if the former is unset, defaulting to UTF-8
assign_from_java_args_if_undef ONTOP_FILE_ENCODING "file.encoding"
: "${ONTOP_FILE_ENCODING:=UTF-8}"

# Assign ONTOP_LOG_CONFIG based on ONTOP_JAVA_ARGS if the former is unset, defaulting to /opt/ontop/log/logback.xml
assign_from_java_args_if_undef ONTOP_LOG_CONFIG "logging.config" "logback.configurationFile"
: "${ONTOP_LOG_CONFIG:=${ONTOP_HOME}/log/logback.xml}"

# Assign ONTOP_LOG_LEVEL based on legacy ONTOP_DEBUG if the former is unset, or report a warning if both are set
if [ -z "${ONTOP_LOG_LEVEL}" ]; then
  if [ "${ONTOP_DEBUG-false}" != "false" ]; then
    export ONTOP_LOG_LEVEL="debug"
  else
    export ONTOP_LOG_LEVEL="info"
  fi
elif [ ${ONTOP_DEBUG+x} ]; then
  echo "WARNING: environment variable ONTOP_DEBUG ignored due to ONTOP_LOG_LEVEL being specified"
fi

# Inject JMX related options into ONTOP_JAVA_ARGS if ONTOP_CONFIGURE_JMX is set
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

# Process ONTOP_WAIT_FOR, waiting for availability of all host:port listed in its value
if [ "${ONTOP_WAIT_FOR}" ]; then
  echo "INFO: waiting for availability of TCP ports ${ONTOP_WAIT_FOR}${ONTOP_WAIT_TIMEOUT+" (max ${ONTOP_WAIT_TIMEOUT}s)"}"
  TS=$(date +%s)
  for HOST_PORT in ${ONTOP_WAIT_FOR}; do
    HOST=${HOST_PORT%%:*}
    PORT=${HOST_PORT#*:}
    while true; do
      ELAPSED=$(( $(date +%s) - TS ))
      if nc -w1 -z "${HOST}" "${PORT}"; then
        echo "INFO: port ${HOST_PORT} available after ${ELAPSED}s"
        break
      elif [ "${ONTOP_WAIT_TIMEOUT}" ] && [ "${ELAPSED}" -gt "${ONTOP_WAIT_TIMEOUT}" ]; then
        echo "ERROR: port ${HOST_PORT} not available within required ${ONTOP_WAIT_TIMEOUT}s"
        exit 1
      else
        sleep 1
      fi
    done
  done
fi

# Run Ontop replacing the shell running this script so to proper handle signals
# shellcheck disable=SC2086
exec java \
  ${ONTOP_JAVA_ARGS} \
  "-Dfile.encoding=${ONTOP_FILE_ENCODING}" \
  "-Dlogging.config=${ONTOP_LOG_CONFIG}" \
  "-Dlogback.configurationFile=${ONTOP_LOG_CONFIG}" \
  -cp "${ONTOP_HOME}/lib/*:${ONTOP_HOME}/jdbc/*" \
  it.unibz.inf.ontop.cli.Ontop "$@"
