FROM openjdk:8-jre
VOLUME /tmp
RUN mkdir -p /opt/ontop
COPY . /opt/ontop/
EXPOSE 8080
WORKDIR /opt/ontop
ENTRYPOINT ./entrypoint.sh
