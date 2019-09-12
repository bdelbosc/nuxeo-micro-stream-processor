FROM openjdk:11-jdk-slim

WORKDIR                 /work/
COPY target/libs        /work/libs
ARG JAR_FILE=acme-processor.jar
COPY target/${JAR_FILE} /work/acme-processor.jar
EXPOSE 8080

CMD java -Xmx64m -Xms64m -Dlog4j.configurationFile=msp-log4j2.xml -jar /work/acme-processor.jar
