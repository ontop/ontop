FROM postgres:14.2

RUN apt-get  update && apt-get install -y wget

# copying all SQL files to this folder will automatically put them in the default DB
COPY sql/*.sql /docker-entrypoint-initdb.d/