FROM openjdk:11-jdk-slim

WORKDIR                /work/
COPY target/libs       /work/libs
COPY target/*.jar      /work/acme-rest.jar
EXPOSE 8080

CMD java -Xmx64m -Xms64m -jar /work/acme-rest.jar
