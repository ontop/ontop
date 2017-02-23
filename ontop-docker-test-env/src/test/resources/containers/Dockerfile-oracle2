
FROM wnameless/oracle-xe-11g


# copying all SQL files to this folder will automatically put them in the default DB
COPY sql/oracle/*.sql /docker-entrypoint-initdb.d/



# docker build -t ontop_oracle .


# docker pull wnameless/oracle-xe-11g

# based one https://store.docker.com/community/images/wnameless/oracle-xe-11g

# connect with

# hostname: localhost
# port: 49161
# sid: xe
# username: system
# password: oracle

# docker run -d -p 49160:22 -p 49161:1521 wnameless/oracle-xe-11g

# to be connected remotely
# docker run -d -p 49160:22 -p 49161:1521 -e ORACLE_ALLOW_REMOTE=true wnameless/oracle-xe-11g


# ssh root@localhost -p 49160
# password: admin

# password for SYS & SYSTEM = oracle

