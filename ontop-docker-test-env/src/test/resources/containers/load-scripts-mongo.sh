#!/bin/bash
for f in /tmp/sql_scripts/*.js; do mongo 127.0.0.1:27017 $f; done


