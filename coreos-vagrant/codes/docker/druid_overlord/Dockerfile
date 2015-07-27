FROM centos
MAINTAINER SAEKI Yoshiyasu <@laclefyoshi>

WORKDIR /tmp
RUN yum install -y java-1.7.0-openjdk-devel.x86_64 tar.x86_64 mysql-connector-java
ENV JAVA_HOME /usr/lib/jvm/java
RUN curl -O http://static.druid.io/artifacts/releases/druid-0.7.1.1-bin.tar.gz
RUN tar zxvf druid-0.7.1.1-bin.tar.gz
RUN mv druid-0.7.1.1 /opt/

WORKDIR /opt/druid-0.7.1.1
ADD start_druid_overlord.sh /opt/druid-0.7.1.1/
ENTRYPOINT ["bash", "./start_druid_overlord.sh"]
EXPOSE 8080

