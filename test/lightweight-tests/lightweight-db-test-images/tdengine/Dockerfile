FROM tdengine/tdengine:3.3.5.0

RUN mkdir /var/custom/

COPY ./sql/db.sql /var/custom/
COPY ./load-scripts-tde.sh /var/custom/

ENTRYPOINT ["bash", "/var/custom/load-scripts-tde.sh"]
