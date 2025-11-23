# Ontop + DuckDB example image with RDF-star mappings
ARG ONTOP_BASE_IMAGE=ontop/ontop:latest
FROM ${ONTOP_BASE_IMAGE}

USER root
ARG DUCKDB_JDBC_VERSION=0.10.3

RUN apt-get update \
    && apt-get install -y --no-install-recommends curl ca-certificates gzip \
    && rm -rf /var/lib/apt/lists/*

RUN curl -fsSL https://install.duckdb.org | sh \
    && cp /root/.duckdb/cli/latest/duckdb /usr/local/bin/duckdb \
    && chmod +x /usr/local/bin/duckdb \
    && rm -rf /root/.duckdb

RUN mkdir -p /opt/ontop/input

COPY duckdb.sql /opt/ontop/input/duckdb.sql
COPY duckdb.properties /opt/ontop/input/duckdb.properties
COPY mapping-rdfstar.obda /opt/ontop/input/mapping-rdfstar.obda

RUN duckdb /opt/ontop/input/duckdb.db < /opt/ontop/input/duckdb.sql \
    && curl -fsSL "https://repo1.maven.org/maven2/org/duckdb/duckdb_jdbc/${DUCKDB_JDBC_VERSION}/duckdb_jdbc-${DUCKDB_JDBC_VERSION}.jar" \
        -o /opt/ontop/lib/duckdb_jdbc.jar \
    && chown -R ontop:ontop /opt/ontop/input /opt/ontop/lib/duckdb_jdbc.jar

USER ontop
ENV ONTOP_MAPPING_FILE=/opt/ontop/input/mapping-rdfstar.obda
ENV ONTOP_PROPERTIES_FILE=/opt/ontop/input/duckdb.properties
ENV ONTOP_DB_DRIVER=org.duckdb.DuckDBDriver
ENV ONTOP_DB_URL=jdbc:duckdb:/opt/ontop/input/duckdb.db

# The base image entrypoint already launches `ontop endpoint`.

