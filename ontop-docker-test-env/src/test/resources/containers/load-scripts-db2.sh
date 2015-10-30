#!/bin/bash
db2 "create db ontop"
db2 "connect to ontop"
for f in /tmp/sql_scripts/*.sql; do db2 -tmf $f; done
