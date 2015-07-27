FROM centos
MAINTAINER SAEKI Yoshiyasu <@laclefyoshi>

WORKDIR /tmp
RUN yum install -y java-1.7.0-openjdk-devel.x86_64 tar.x86_64
ENV JAVA_HOME /usr/lib/jvm/java
RUN curl -O http://ftp.tsukuba.wide.ad.jp/software/apache/kafka/0.8.2.1/kafka_2.10-0.8.2.1.tgz
RUN tar zxvf kafka_2.10-0.8.2.1.tgz
RUN mkdir -p /tmp/kafka-logs
RUN mv kafka_2.10-0.8.2.1 /opt/

WORKDIR /opt/kafka_2.10-0.8.2.1
RUN mkdir logs
RUN touch logs/server.log
ADD start_kafka_broker.sh /opt/kafka_2.10-0.8.2.1/
ENTRYPOINT ["bash", "./start_kafka_broker.sh"]
EXPOSE 9092

