FROM openjdk:11-jdk-slim

WORKDIR                /work/
COPY target/libs       /work/libs
ARG JAR_FILE=acme-rest.jar
COPY target/${JAR_FILE} /work/acme-rest.jar
EXPOSE 8080

CMD java -Xmx64m -Xms64m -jar /work/acme-rest.jar
