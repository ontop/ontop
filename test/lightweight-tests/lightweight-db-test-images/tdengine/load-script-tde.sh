#!/bin/sh
# /tini -s -- /usr/bin/entrypoint.sh

# start the tdengine server and adapter
taosadapter &
taosd &
sleep 5

# create the database
taos -f /var/custom/db.sql

# restart server
pkill taosd
sleep 5
taosd