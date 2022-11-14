FROM ibmcom/db2:11.5.7.0a

RUN mkdir /var/custom
RUN mkdir -p /tmp/sql_scripts/

COPY /sql/*.sql /tmp/sql_scripts/
COPY load-scripts-db2.sh /var/custom/

RUN chmod a+x /var/custom/load-scripts-db2.sh