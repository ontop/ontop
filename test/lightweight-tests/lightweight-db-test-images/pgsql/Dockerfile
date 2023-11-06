FROM postgres:14.2

RUN apt-get  update && apt-get install -y wget postgresql-14-postgis-3

# copying all SQL files to this folder will automatically put them in the default DB
COPY sql/*.sql /docker-entrypoint-initdb.d/