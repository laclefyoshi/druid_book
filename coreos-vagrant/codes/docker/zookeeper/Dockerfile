FROM centos
MAINTAINER SAEKI Yoshiyasu <@laclefyoshi>

WORKDIR /tmp
RUN yum install -y java-1.7.0-openjdk-devel.x86_64 tar.x86_64
ENV JAVA_HOME /usr/lib/jvm/java
RUN curl -O http://ftp.tsukuba.wide.ad.jp/software/apache/zookeeper/zookeeper-3.4.6/zookeeper-3.4.6.tar.gz
RUN tar zxvf zookeeper-3.4.6.tar.gz
RUN cp zookeeper-3.4.6/conf/zoo_sample.cfg zookeeper-3.4.6/conf/zoo.cfg
RUN mkdir -p /tmp/zookeeper
RUN mv zookeeper-3.4.6 /opt/

WORKDIR /opt/zookeeper-3.4.6
ENTRYPOINT ["./bin/zkServer.sh"]
CMD ["start-foreground"]
EXPOSE 2181

