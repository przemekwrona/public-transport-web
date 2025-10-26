FROM openjdk:17-jdk-slim-buster
MAINTAINER igeolab.pl
WORKDIR /app
EXPOSE 8080

ENV PROFILE=''
ENV JAVA_OPTS=''

ENV SPRING_DB_URL = ''
ENV SPRING_DB_USER = ''
ENV SPRING_DB_PASSWORD = ''

ENV GEOAPIFY_KEY=''

COPY target/public-transport-web-server-*.jar public-transport-web-server.jar

ENTRYPOINT java $JAVA_OPTS -jar -Dspring.profiles.active=$PROFILE -Dspring.datasource.url=$SPRING_DB_URL -Dspring.datasource.username=$SPRING_DB_USER -Dspring.datasource.password=$SPRING_DB_PASSWORD -Dgeoapify.client.key=$GEOAPIFY_KEY public-transport-web-server.jar
