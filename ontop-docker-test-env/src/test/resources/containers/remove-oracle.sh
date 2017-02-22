#/bin/bash
docker kill ontop_oracle_running
#-v is used to remove also the associated volume
docker rm -v ontop_oracle_running
docker rmi ontop_oracle
