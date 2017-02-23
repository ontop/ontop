#/bin/bash
docker kill ontop_oracle2_running
#-v is used to remove also the associated volume
docker rm -v ontop_oracle2_running
docker rmi ontop_oracle2
