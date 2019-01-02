FROM openjdk:8-jdk-alpine

MAINTAINER Guohui Xiao <gh.xiao@gmail.com>

VOLUME /tmp
RUN mkdir -p /opt/ontop
COPY . /opt/ontop/
WORKDIR /opt/ontop
ENTRYPOINT java -cp ./lib/*:./jdbc/* -Dlogback.configurationFile=./log/logback.xml it.unibz.inf.ontop.cli.Ontop endpoint --ontology=$ONTOLOGY_FILE --mapping=$MAPPING_FILE --properties=$PROPERTIES_FILE
