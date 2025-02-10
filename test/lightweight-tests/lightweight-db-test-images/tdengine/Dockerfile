FROM tdengine/tdengine:3.3.5.0

RUN mkdir /var/custom/

COPY ./sql/db.sql /var/custom/
COPY ./load-scripts-tde.sh /var/custom/
# COPY ./libtaos.so.3.3.5.0 /usr/local/lib/

# RUN cd /usr/local/lib/ \
#     && ln -s libtaos.so.3.3.5.0 libtaos.so.1 \
#     && ln -s libtaos.so.1 libtaos.so

# ENV LD_LIBRARY_PATH=/usr/local/lib/$LD_LIBRARY_PATH

ENTRYPOINT ["bash", "/var/custom/load-scripts-tde.sh"]
