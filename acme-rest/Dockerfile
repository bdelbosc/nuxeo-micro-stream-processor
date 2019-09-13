FROM openjdk:11-jdk-slim

WORKDIR /work/
COPY src/main/resources/log4j2.xml /work/acme-log4j2.xml
COPY target/libs                   /work/libs
ARG JAR_FILE=acme-rest.jar
COPY target/${JAR_FILE}            /work/acme-rest.jar
EXPOSE 8080

CMD java -Xmx64m -Xms64m \
  -Dlog4j.configurationFile=acme-log4j2.xml \
  -agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=*:8787 \
  -jar /work/acme-rest.jar
