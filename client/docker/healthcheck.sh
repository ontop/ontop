#!/bin/sh

# Exit code reported by healthcheck script (start from 0=success)
STATUS=0

# Select default healthchecks to perform, if ONTOP_HEALTHCHECK environment variable is unset:
# we default to "port" for Ontop endpoint command (so to detect when Ontop has started), and to
# "process" otherwise, so to check that whatever other Ontop command was started is still properly
# running ("process" is the only healthcheck that works for any Ontop command).
if [ -z ${ONTOP_HEALTHCHECK} ]; then
  PROCESS_STATE=$( ps -p 1 -o stat=,command= )
  if [ "$( expr "${PROCESS_STATE}" : ".*it\.unibz\.inf\.ontop\.cli\.Ontop endpoint.*" )" -gt 0 ]; then
    ONTOP_HEALTHCHECK="port"
  else
    ONTOP_HEALTHCHECK="process"
  fi
fi

# Healthcheck function, testing process state, port reachability, and query functionality
# according to space-spearated checks configured by ONTOP_HEALTHCHECK environment variable
check() {
  for CHECK in $ONTOP_HEALTHCHECK; do
    case "${CHECK}" in
      process)
        # Check process status via ps: PID 1 process must exist & its state must start with R|S|D
        # (see https://man7.org/linux/man-pages/man1/ps.1.html "Process State Codes")
        case "${PROCESS_STATE:=$( ps -p 1 -o stat= )}" in
          R*|S*|D*) echo -n "Process running. ";;
          "")       echo -n "Process not found. "; STATUS=1;;
          *)        echo -n "Process in unexpected state '$( ps -p 1 -o stat= )'. "; STATUS=1;;
        esac
        ;;
      port)
        # Check port 8080 reachability via nc (netcat)
        if nc -w1 -z localhost 8080
        then echo -n "Port reachable. "
        else echo -n "Port not reachable. "; STATUS=1
        fi
        ;;
      query)
        # Check query functionality by sending (wget GET) a probe SPARQL query that will test both
        # query rewriting and DB availability. Default query is 'SELECT * {}' that is mapped to SQL
        # query 'SELECT 1 AS uselessVariable FROM ( SELECT 1 ) tdummy'
        : ${ONTOP_HEALTHCHECK_QUERY:=SELECT * { \} %23 HEALTHCHECK QUERY %23}
        if wget -q -t1 --spider "http://localhost:8080/sparql?query=${ONTOP_HEALTHCHECK_QUERY}";
        then echo -n "Queries working. "
        else echo -n "Queries not working. "; STATUS=1
        fi
        ;;
      *)
        # Unrecognize check: will cause the healthcheck to fail
        echo -n "Invalid ONTOP_HEALTHCHECK value '$CHECK'. "; STATUS=1
        ;;
    esac
  done
}

# Execute healthcheck differently based on ONTOP_HEALTHCHECK_POSTINTERVAL environment variable
if [ -z ${ONTOP_HEALTHCHECK_POSTINTERVAL} ]; then
  # If undefined or empty, always run the healthcheck
  check
else
  # Otherwise, always run the healthcheck if previous healthcheck is missing or resulted in a failure,
  # otherwise wait for at least ONTOP_HEALTHCHECK_POSTINTERVAL seconds. This allows setting a short
  # healthcheck interval in Docker (e.g., 1 second) to quickly determine whether Ontop has started, and
  # then switch to a longer interval as long as Ontop is healthy (this emulates a Docker feature
  # requested by users and relevant when using Docker Compose and waiting for Ontop to become healthy).
  TS_FILE="/tmp/.ontop_healthcheck_ts"
  TS_LAST=$( date -r "${TS_FILE}" +%s 2> /dev/null )
  ELAPSED=$(( $(date +%s) - ${TS_LAST:-0} ))
  if [ ${ELAPSED} -lt ${ONTOP_HEALTHCHECK_POSTINTERVAL} ]; then
    echo -n "Healthcheck postponed (${ELAPSED}s elapsed since last healthcheck < ${ONTOP_HEALTHCHECK_POSTINTERVAL}s). "
  else
    check
    if [ ${STATUS} -eq 0 ]; then
      touch "${TS_FILE}";
    else
      rm -f "${TS_FILE}";
    fi
  fi
fi

# Add exit code to message and return it to Docker
echo "Ontop status: ${STATUS}"
exit ${STATUS}
