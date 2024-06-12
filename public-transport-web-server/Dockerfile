FROM openjdk:17-jdk-slim-buster
MAINTAINER igeolab.pl
WORKDIR /app

ENV PROFILE=''

COPY target/public-transport-web-server-*.jar public-transport-web-server.jar

ENTRYPOINT java -jar -Dspring.profiles.active=$PROFILE public-transport-web-server.jar
