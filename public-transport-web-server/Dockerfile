FROM eclipse-temurin:17
MAINTAINER igeolab.pl
WORKDIR /app
EXPOSE 8080

ENV PROFILE=''
ENV JAVA_OPTS=''
ENV GEOAPIFY_KEY=''

COPY target/public-transport-web-server-*.jar public-transport-web-server.jar

ENTRYPOINT java $JAVA_OPTS -jar -Dspring.profiles.active=$PROFILE -Dspring.datasource.username=$SPRING_DATASOURCE_USERNAME -Dspring.datasource.password=$SPRING_DATASOURCE_PASSWORD -Dgeoapify.client.key=$GEOAPIFY_KEY public-transport-web-server.jar
