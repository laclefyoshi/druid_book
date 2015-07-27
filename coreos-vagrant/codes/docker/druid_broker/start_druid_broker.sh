#!/bin/sh

sed -i -e 's/druid.zk.service.host=localhost/druid.zk.service.host=172.17.9.101:2181/' \
    -e 's;druid.metadata.storage.connector.connectURI=jdbc\\:mysql\\://localhost\\:3306/druid;druid.metadata.storage.connector.connectURI=jdbc\\:mysql\\://172.17.9.102\\:3306/druid;' \
    config/_common/common.runtime.properties
sed -i -e 's/#druid.host=localhost/druid.host=172.17.9.103:8080/' \
    -e 's/#druid.port=8082/druid.port=8080/' \
    -e 's/druid.processing.numThreads=1/druid.processing.numThreads=2/' \
    config/broker/runtime.properties

java -Xmx924M -Duser.timezone=UTC -Dfile.encoding=UTF-8 -server \
    -classpath '/opt/druid-0.7.1.1/config/_common:/opt/druid-0.7.1.1/config/broker:/opt/druid-0.7.1.1/lib/*' \
    io.druid.cli.Main server broker

