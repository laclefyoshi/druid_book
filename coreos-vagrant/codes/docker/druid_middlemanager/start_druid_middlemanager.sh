#!/bin/sh

sed -i -e 's/druid.zk.service.host=localhost/druid.zk.service.host=172.17.9.101:2181/' \
    -e 's;druid.metadata.storage.connector.connectURI=jdbc\\:mysql\\://localhost\\:3306/druid;druid.metadata.storage.connector.connectURI=jdbc\\:mysql\\://172.17.9.102\\:3306/druid;' \
    config/_common/common.runtime.properties
cp -r config/overlord config/middlemanager
sed -i -e 's/#druid.host=localhost/druid.host=172.17.9.103:8080/' \
    -e 's/#druid.port=8090/druid.port=8080/' \
    -e 's/druid.service=overlord/druid.service=middlemanager/' \
    config/middlemanager/runtime.properties

echo "" >> config/middlemanager/runtime.properties
echo "druid.worker.ip=172.17.9.103" >> config/middlemanager/runtime.properties
echo "druid.worker.capacity=1" >> config/middlemanager/runtime.properties
echo "druid.indexer.task.chathandler.type=announce" >> config/middlemanager/runtime.properties
echo "druid.indexer.logs.directory=/tmp/druid/log" >> config/middlemanager/runtime.properties
echo "druid.indexer.task.baseTaskDir=/tmp/druid/task" >> config/middlemanager/runtime.properties

java -Xmx924M -Duser.timezone=UTC -Dfile.encoding=UTF-8 -server \
    -classpath '/opt/druid-0.7.1.1/config/_common:/opt/druid-0.7.1.1/config/middlemanager:/opt/druid-0.7.1.1/lib/*' \
    io.druid.cli.Main server middleManager

