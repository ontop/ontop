FROM bitnami/minideb:buster

RUN install_packages curl ca-certificates -y
RUN install_packages wget procps openjdk-11-jdk-headless git python3 -y

WORKDIR /tmp

RUN wget https://archive.apache.org/dist/spark/spark-3.3.1/spark-3.3.1-bin-hadoop3.tgz
RUN tar xvf spark-*
RUN mv spark-3.3.1-bin-hadoop3 /opt/spark

ENV SPARK_HOME=/opt/spark
ENV PATH=${PATH}:${SPARK_HOME}/bin:${SPARK_HOME}/sbin
ENV PYSPARK_PYTHON=/usr/bin/python3

WORKDIR /home

COPY start.sh start.sh
COPY sql/all-spark.sql all-spark.sql

RUN chmod +x start.sh

EXPOSE 8050
EXPOSE 8051
EXPOSE 10000

CMD [ "./start.sh" ]