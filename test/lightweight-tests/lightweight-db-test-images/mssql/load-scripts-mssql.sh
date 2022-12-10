#!/bin/bash
#wait for the SQL Server to come up
sleep 30s

for f in /tmp/sql_scripts/*.sql;
do
    echo Running $f
    /opt/mssql-tools/bin/sqlcmd  -S localhost -U SA -P 'Mssql1.0' -i $f;
done