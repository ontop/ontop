FROM mcr.microsoft.com/mssql/server:2022-latest

# Set this so that it skips prompting you during the mssql-tools package install
ENV ACCEPT_EULA=Y \
    LANG='C.UTF-8' \
    LC_ALL='C.UTF-8' \
    LANGUAGE='C.UTF-8'
ENV SA_PASSWORD='Mssql1.0'
ENV DEBIAN_FRONTEND noninteractive

# Install


RUN mkdir -p /tmp/sql_scripts/
USER root
RUN mkdir -p /usr/work/
RUN chown mssql /usr/work
USER mssql
WORKDIR /usr/work

COPY sql/*.sql /tmp/sql_scripts/

COPY load-scripts-mssql.sh /usr/work/
USER root
RUN chmod +x /usr/work/load-scripts-mssql.sh
USER mssql

EXPOSE 1433

RUN /opt/mssql/bin/sqlservr & /usr/work/load-scripts-mssql.sh