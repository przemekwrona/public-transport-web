release: mvn -f public-transport-web-server/pom.xml liquibase:update -DskipTests
web: java -Dspring.profiles.active=prod -jar public-transport-web-server/target/public-transport-web-server-0.0.1-SNAPSHOT.jar
