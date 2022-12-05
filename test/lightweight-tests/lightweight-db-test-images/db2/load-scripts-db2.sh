#!/bin/bash
for f in /tmp/sql_scripts/*.sql;
 do
 echo Running $f;
 su - db2inst1 -c 'db2 -tmf '$f
 done
