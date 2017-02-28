#!/bin/bash
for f in /tmp/sql_scripts/*.sql;
 do
 echo Running $f;
 db2 -tmf $f;
 done


