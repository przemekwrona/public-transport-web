FROM openjdk:17-jdk-slim-buster
MAINTAINER igeolab.pl
WORKDIR /app
EXPOSE 8080

ENV PROFILE=''
ENV JAVA_OPTS=''

ENV DB_URL = ''
ENV DB_USER = ''
ENV DB_PASSSWORD = ''

ENV GEOAPIFY_KEY=''

COPY target/public-transport-web-server-*.jar public-transport-web-server.jar

ENTRYPOINT java $JAVA_OPTS -jar -Dspring.profiles.active=$PROFILE -Dspring.datasource.url=$DB_URL -Dspring.datasource.username=$DB_USER -Dspring.datasource.password=$DB_PASSSWORD -Dgeoapify.client.key=$GEOAPIFY_KEY public-transport-web-server.jar
