#!/bin/bash

#
# ONTOP DOCKER ENTRYPOINT SCRIPT
#
# The script runs 'ontop endpoint <args...>' by default (i.e., called without arguments or if first one is
# an -option), or alternatively the arbitrary command specified, which may a different ontop command as well
# as bash or any other command the user wants to run within the Ontop Docker image.
#

# Echo each statement being run as part of the script, if ONTOP_LOG_ENTRYPOINT is set
[ "${ONTOP_LOG_ENTRYPOINT-false}" != "false" ] && set -x -v

# Check argument list
if [ $# -eq 0 ] || [ "${1#-}" != "$1" ]; then
  set -- "endpoint" "$@"  # run 'ontop endpoint' with processed arg list, if there are no arguments or they start with an '-option'
elif [ "$1" = "ontop" ]; then
  shift  # run 'ontop' with processed arg list, if it starts with 'ontop' (which is then discarded)
else
  exec "$@"  # run whatever other command (e.g., bash) has been specified
fi

# Reset status of healtcheck.sh (for 'query' healtcheck strategy)
rm -f /tmp/.healthcheck

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

# Delegate to Ontop startup script
# shellcheck disable=SC1091
. /opt/ontop/ontop
