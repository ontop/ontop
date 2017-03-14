# build with
# docker build -t ontop_psql .
# run with   
# docker run --name ontop_psql_running -p 5432:5432 -e POSTGRES_PASSWORD=postgres2 -d ontop_psql

FROM postgres:latest

# copying all SQL files to this folder will automatically put them in the default DB
COPY sql/postgres/*.sql /docker-entrypoint-initdb.d/

#COPY init_docker_postgres.sh /docker-entrypoint-initdb.d/

COPY sql/postgres/*.dump /tmp/sql_scripts/

COPY load-scripts-postgres.sh /docker-entrypoint-initdb.d/
