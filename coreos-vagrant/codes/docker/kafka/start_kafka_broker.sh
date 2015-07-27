#!/bin/sh
sed -i -e 's/zookeeper.connect=localhost:2181/zookeeper.connect=172.17.9.101:2181/' \
    -e 's/#advertised.host.name=<hostname routable by clients>/advertised.host.name=172.17.9.104/' \
    config/server.properties
echo "" >> config/server.properties
echo "delete.topic.enable=true" >> config/server.properties

sed -i -e 's/-Xmx1G -Xms1G/-Xmx2G -Xms1G/' \
    bin/kafka-server-start.sh

./bin/kafka-server-start.sh -daemon config/server.properties
echo "starting kafka broker..."
sleep 120

./bin/kafka-topics.sh --delete --zookeeper 172.17.9.101:2181 \
    --topic test-topic || echo "OK"
./bin/kafka-topics.sh --create --zookeeper 172.17.9.101:2181 \
    --replication-factor 1 --partitions 2 --topic test-topic
tailf ./logs/server.log

