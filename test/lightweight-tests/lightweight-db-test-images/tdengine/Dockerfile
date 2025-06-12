FROM tdengine/tdengine:3.3.5.8

RUN mkdir /var/custom/

COPY sql/* /var/custom/
COPY ./load-scripts-tde.sh /var/custom/

ENTRYPOINT ["bash", "/var/custom/load-scripts-tde.sh"]
