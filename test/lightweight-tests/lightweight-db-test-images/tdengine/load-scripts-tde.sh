#!/bin/sh
# /tini -s -- /usr/bin/entrypoint.sh 

# start the tdengine server and rest endpoint
taosadapter &
taosd &
sleep 5

# create the database
taos -f /var/custom/db.sql
# sleep 5

# restat server
pkill taosd
sleep 5
taosd