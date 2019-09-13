FROM openjdk:11-jdk-slim


ENTRYPOINT ["java", "-Xmx24m", "-Xms24m", "-Xdebug", "-Dlog4j.configurationFile=msp-log4j2.xml", "-XX:+FlightRecorder", "-agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=*:8787", "-cp", "/work/libs/*", "-Dlog4j.configurationFile=tools-log4j2.xml", "org.nuxeo.lib.stream.tools.Main"]

WORKDIR /work/
COPY src/main/resources/log4j2.xml /work/msp-log4j2.xml
COPY target/libs                   /work/libs
