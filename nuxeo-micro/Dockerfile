FROM openjdk:11-jdk-slim

ENTRYPOINT ["java", "-Xdebug", "-agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=*:8787", "-cp", "/work/libs/*", "-Dlog4j.configurationFile=tools-log4j2.xml", "org.nuxeo.lib.stream.tools.Main"]

EXPOSE 8787
WORKDIR                 /work/
COPY target/libs        /work/libs
