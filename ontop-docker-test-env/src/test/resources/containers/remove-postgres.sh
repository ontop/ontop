#/bin/bash
docker kill ontop_postgres_running
docker rm -v ontop_postgres_running
docker rmi ontop_postgres
