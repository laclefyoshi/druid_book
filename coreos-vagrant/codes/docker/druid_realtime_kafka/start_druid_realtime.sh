#!/bin/sh

sed -i -e 's/druid.zk.service.host=localhost/druid.zk.service.host=172.17.9.101:2181/' \
    -e 's;druid.metadata.storage.connector.connectURI=jdbc\\:mysql\\://localhost\\:3306/druid;druid.metadata.storage.connector.connectURI=jdbc\\:mysql\\://172.17.9.102\\:3306/druid;' \
    config/_common/common.runtime.properties
sed -i -e 's/#druid.host=localhost/druid.host=172.17.9.103:8080/' \
    -e 's/#druid.port=8084/druid.port=8080/' \
    config/realtime/runtime.properties

java -Xmx256m -Duser.timezone=UTC -Dfile.encoding=UTF-8 \
    -classpath '/opt/druid-0.7.1.1/config/_common:/opt/druid-0.7.1.1/lib/*' \
    io.druid.cli.Main tools metadata-init \
    --base druid --connectURI jdbc:mysql://172.17.8.102:3306/druid --user druid --password diurd

java -Xmx924M -Duser.timezone=UTC -Dfile.encoding=UTF-8 -server \
    -Ddruid.realtime.specFile=/opt/druid-0.7.1.1/config/realtime/realtime.spec \
    -classpath '/opt/druid-0.7.1.1/config/_common:/opt/druid-0.7.1.1/config/realtime:/opt/druid-0.7.1.1/lib/*' \
    io.druid.cli.Main server realtime

