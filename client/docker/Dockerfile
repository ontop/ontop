FROM openjdk:8-jdk-alpine
RUN apk add --no-cache bash
VOLUME /tmp
RUN mkdir -p /opt/ontop
COPY . /opt/ontop/
EXPOSE 8080
WORKDIR /opt/ontop
ENTRYPOINT ./entrypoint.sh
