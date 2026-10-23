FROM eclipse-temurin:17
MAINTAINER igeolab.pl
WORKDIR /app
EXPOSE 8080

ENV PROFILE=''
ENV JAVA_OPTS=''
ENV GEOAPIFY_KEY=''

COPY target/public-transport-web-server-*.jar public-transport-web-server.jar
COPY entrypoint.sh entrypoint.sh
RUN chmod +x entrypoint.sh

ENTRYPOINT ["./entrypoint.sh"]
