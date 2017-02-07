#!/bin/bash
for f in /tmp/sql_scripts/*.sql; do db2 -tmf $f; done


