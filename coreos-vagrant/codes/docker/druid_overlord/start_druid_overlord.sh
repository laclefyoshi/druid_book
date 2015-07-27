#!/bin/sh

sed -i -e 's/druid.zk.service.host=localhost/druid.zk.service.host=172.17.9.101:2181/' \
    -e 's;druid.metadata.storage.connector.connectURI=jdbc\\:mysql\\://localhost\\:3306/druid;druid.metadata.storage.connector.connectURI=jdbc\\:mysql\\://172.17.9.102\\:3306/druid;' \
    config/_common/common.runtime.properties
sed -i -e 's/#druid.host=localhost/druid.host=172.17.9.103:8080/' \
    -e 's/#druid.port=8090/druid.port=8080/' \
    config/overlord/runtime.properties
echo "" >> config/overlord/runtime.properties
echo "druid.indexer.runner.type=remote" >> config/overlord/runtime.properties
echo "druid.indexer.storage.type=metadata" >> config/overlord/runtime.properties

java -Xmx924M -Duser.timezone=UTC -Dfile.encoding=UTF-8 -server \
    -classpath '/opt/druid-0.7.1.1/config/_common:/opt/druid-0.7.1.1/config/overlord:/opt/druid-0.7.1.1/lib/*' \
    io.druid.cli.Main server overlord

