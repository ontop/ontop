#!/bin/sh

start-master.sh \
	--host localhost \
	--port 7077 \
	--webui-port 8050

start-worker.sh \
	spark://localhost:7077 \
	--host localhost \
	--port 7077 \
	--webui-port 8051

spark-sql \
	--name SPARKSQL-INSERTIONS \
	--master spark://localhost:7077 \
	--deploy-mode client  \
	-f ./all-spark.sql

start-thriftserver.sh \
	--name THRIFTSERVER \
	--master spark://localhost:7077 \
	--deploy-mode client \
	--hiveconf hive.server2.thrift.port=10000 \
	--hiveconf hive.server2.thrift.bind.host=0.0.0.0 \

tail -f /dev/null