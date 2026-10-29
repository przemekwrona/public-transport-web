#!/bin/sh
set -e

# Sprawdź, czy zmienna z certyfikatem została przekazana
if [ -n "$KEYCLOAK_CERT_PEM" ]; then
    echo "Configuring a custom Keycloak certificate for this environment..."

    # Zapisz certyfikat ze zmiennej środowiskowej do pliku tymczasowego
    printf '%s\n' "$KEYCLOAK_CERT_PEM" | sed 's/\\n/\n/g' > /tmp/keycloak-cert.pem

    # Zacznij od urzędów Javy, potem dołóż certyfikat Keycloaka
    cp "$JAVA_HOME/lib/security/cacerts" /tmp/custom-truststore.jks
    keytool -import -trustcacerts \
        -keystore /tmp/custom-truststore.jks \
        -storepass changeit -noprompt \
        -alias keycloak-env \
        -file /tmp/keycloak-cert.pem
fi

exec java $JAVA_OPTS -jar \
  -Dspring.profiles.active="$PROFILE" \
  -Dspring.datasource.username="$SPRING_DATASOURCE_USERNAME" \
  -Dspring.datasource.password="$SPRING_DATASOURCE_PASSWORD" \
  -Dgeoapify.client.key="$GEOAPIFY_KEY" \
  public-transport-web-server.jar
