FROM openjdk:17-jdk-slim-buster
MAINTAINER igeolab.pl
WORKDIR /app
EXPOSE 8080

ENV PROFILE=''
ENV JAVA_OPTS=''

COPY target/public-transport-web-server-*.jar public-transport-web-server.jar

ENTRYPOINT java $JAVA_OPTS -jar -Dspring.profiles.active=$PROFILE public-transport-web-server.jar
