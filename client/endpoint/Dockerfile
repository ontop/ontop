FROM openjdk:8-jdk-alpine
VOLUME /tmp
COPY target/ontop-endpoint-3.0.0-beta-3-SNAPSHOT.jar /
ENTRYPOINT java -jar /ontop-endpoint-3.0.0-beta-3-SNAPSHOT.jar --t=$ONTOLOGY_FILE --m=$MAPPING_FILE --p=$PROPERTIES_FILE
