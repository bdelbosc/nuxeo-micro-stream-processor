FROM openjdk:11-jdk-slim

WORKDIR /work/
COPY src/main/resources/log4j2.xml /work/acme-log4j2.xml
COPY target/libs                   /work/libs
ARG JAR_FILE=acme-processor.jar
COPY target/${JAR_FILE}            /work/acme-processor.jar
EXPOSE 8080

CMD java -Xmx64m -Xms64m \
  -Dlog4j.configurationFile=acme-log4j2.xml \
  -XX:NativeMemoryTracking=summary \
  -XX:ActiveProcessorCount=2 \
  -XX:+FlightRecorder \
  -agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=*:8787 \
  -jar /work/acme-processor.jar
