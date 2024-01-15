ARG MYSQL_VERSION
FROM mysql:${MYSQL_VERSION}

# copying all SQL files to this folder will automatically put them in the default DB
COPY /sql/*.sql /docker-entrypoint-initdb.d/