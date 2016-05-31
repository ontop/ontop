#/bin/bash

for f in /tmp/sql_scripts/*.sql
do
	echo Running $f
  sqlplus system/oracle < $f
done
