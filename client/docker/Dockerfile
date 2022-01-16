FROM openjdk:11-jre-buster
VOLUME /tmp
RUN mkdir -p /opt/ontop
COPY . /opt/ontop/
EXPOSE 8080
WORKDIR /opt/ontop
ENTRYPOINT ./entrypoint.sh
