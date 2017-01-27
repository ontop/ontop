#!/bin/bash
db2 "create db ontop"
db2 "connect to ontop"
for f in /tmp/sql_scripts/all/*.sql; do db2 -tmf $f; done
db2 "commit work"
db2 "connect reset"
for f in /tmp/sql_scripts/db2/*.sql; do db2 -tmf $f; done
