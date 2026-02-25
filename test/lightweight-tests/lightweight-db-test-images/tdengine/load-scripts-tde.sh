#!/bin/sh
# /tini -s -- /usr/bin/entrypoint.sh 

# start the tdengine server and rest endpoint
taosadapter &
taosd &
sleep 5

# create the database
taos -f /var/custom/db_functions.sql
taos -f /var/custom/db_constraints.sql
# sleep 5

# restat server
pkill taosd
sleep 10
taosd