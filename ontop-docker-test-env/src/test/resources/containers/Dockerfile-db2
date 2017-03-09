
FROM ibmcom/db2express-c

RUN mkdir -p /tmp/sql_scripts/

COPY sql/db2/*.sql /tmp/sql_scripts/
COPY load-scripts-db2.sh /home/db2inst1/

RUN su - db2inst1 -c "db2start && /home/db2inst1/load-scripts-db2.sh" \
	&& su - db2inst1 -c "db2stop force"

#docker exec -it db2expc  bash
#db2expc, a container name created while starting Docker container
#Then, please switch to the default db2 instance user db2inst1 to create a sample database if you want :
#$ su - db2inst1
#$ db2sampl

