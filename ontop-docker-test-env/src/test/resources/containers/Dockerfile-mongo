FROM mongo:latest

# change the directory where data will be stored
RUN mkdir -p /data/mongo_data \
    && chown -R  mongodb:mongodb /data/mongo_data \
     && echo "dbpath = /data/mongo_data" > /etc/mongodb.conf

COPY load-scripts-mongo.sh /data/mongo_data

RUN mkdir -p /tmp/sql_scripts/
COPY sql/mongo/*.js /tmp/sql_scripts/


RUN mongod --fork --logpath /var/log/mongodb.log --dbpath /data/mongo_data \
 &&  bash /data/mongo_data/load-scripts-mongo.sh && mongod --dbpath /data/mongo_data --shutdown \
 && chown -R mongodb /data/mongo_data

# RUN mongod --fork --logpath /var/log/mongodb.log \
# && sleep 30s && mongo --shell /data/db/scripts/primer-dataset.js && mongod --shutdown
# mongod && mongo --shell /data/db/scripts/primer-dataset.js

# Make the new dir a VOLUME to persists it
VOLUME /data/mongo_data

# docker run --name some-mongo -d mongo
# port 27017 is already exposed

CMD ["mongod", "--config", "/etc/mongodb.conf"]