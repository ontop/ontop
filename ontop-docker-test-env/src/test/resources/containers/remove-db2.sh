#/bin/bash
docker kill ontop_db2_running
docker rm -v ontop_db2_running
docker rmi ontop_db2
