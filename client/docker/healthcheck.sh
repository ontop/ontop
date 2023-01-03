#!/bin/sh

#
# ONTOP HEALTHCHECK SCRIPT
#
# Docker healtcheck script testing Ontop health based on (i) process state, (ii) port reachability,
# (iii) probe query executed successfully. The script is controlled by three environment variables
# documented in README.md:
# - ${ONTOP_HEALTHCHECK}: selects check 'process|port|query|queryport' - default process or port
# - ${ONTOP_HEALTHCHECK_QUERY}: specifies the probe query (default: SELECT * {})
# - ${ONTOP_HEALTHCHECK_QUERY_INTERVAL}: specifies how long to wait between successful probe
#   queries (default: 60)
#

# Select default healthchecks to perform, if ${ONTOP_HEALTHCHECK} environment variable is unset:
# we default to "port" for Ontop endpoint command (so to detect when Ontop has started), and to
# "process" otherwise, so to check that whatever other Ontop command was started is still properly
# running ("process" is the only healthcheck that works for any Ontop command).
if [ -z ${ONTOP_HEALTHCHECK} ]; then
  PROCESS_STATE=$( ps -p 1 -o stat=,command= )
  if [ "${PROCESS_STATE#*it.unibz.inf.ontop.cli.Ontop endpoint}" != "${PROCESS_STATE}" ]; then
    ONTOP_HEALTHCHECK="port"
  else
    ONTOP_HEALTHCHECK="process"
  fi
fi

# PROCESS CHECK: check that Ontop process (PID=1) exists & its state starts with R|S|D
# (see https://man7.org/linux/man-pages/man1/ps.1.html "Process State Codes")
check_process() {
  case "${PROCESS_STATE:=$( ps -p 1 -o stat= )}" in
    R*|S*|D*) echo "Process running";;
    "")       echo "Process not found"; exit 1;;
    *)        echo "Process in unexpected state '$( ps -p 1 -o stat= )'"; exit 1;;
  esac
}

# PORT CHECK: lightweight non-invasive test checking that port 8080 is reachable via nc (netcat).
check_port() {
  if nc -w1 -z localhost 8080; then
    echo "Port reachable"
  else
    check_process # check process before complaining about port
    echo "Port not reachable"
    exit 1
  fi
}

# QUERY CHECK: send (wget GET) a probe SPARQL query ${ONTOP_HEALTHCHECK_QUERY} that will test both
# query rewriting and DB availability. Default query is 'SELECT * {}' that is mapped to a dummy
# SQL query 'SELECT 1 AS uselessVariable FROM ( SELECT 1 ) tdummy'
check_query() {
  : ${ONTOP_HEALTHCHECK_QUERY:=SELECT * { \} %23 HEALTHCHECK QUERY %23}
  if wget -q -t1 --spider "http://localhost:8080/sparql?query=${ONTOP_HEALTHCHECK_QUERY}"; then
    echo "Query working"
  else
    check_port # check port before complaining about query
    echo "Query not working"
    exit 1
  fi
}

# QUERY/PORT CHECK: check query and, if successful, revert to just checking port during the next
# ${ONTOP_HEALTHCHECK_QUERY_INTERVAL} seconds. This allows setting a short healthcheck interval in
# Docker (e.g., even 1 second) to quickly determine whether Ontop has started, and then switch to a
# longer interval as long as Ontop is healthy (this emulates a Docker feature requested by users
# and relevant when using Docker Compose and waiting for Ontop to become healthy).
check_query_or_port() {
  TS_LAST=$( date -r /tmp/.healthcheck +%s 2> /dev/null )
  ELAPSED=$(( $(date +%s) - ${TS_LAST:-0} ))
  if [ ${ELAPSED} -lt ${ONTOP_HEALTHCHECK_QUERY_INTERVAL:-60} ]; then
    check_port
  else
    check_query
    touch "/tmp/.healthcheck";
  fi
}

# Execute the check identified by ${ONTOP_HEALTHCHECK}, returning 1 on failure (or unknown check)
case "${ONTOP_HEALTHCHECK}" in
  process)   check_process;;
  port)      check_port;;
  query)     [ ${ONTOP_HEALTHCHECK_QUERY_INTERVAL:-60} -gt 0 ] && check_query_or_port || check_query;;
  *)         echo "Invalid check '${ONTOP_HEALTHCHECK}'"; exit 1;;
esac
