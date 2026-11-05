package pl.wrona.webserver;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.crypto.RSASSASigner;
import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.KeyUse;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jose.jwk.gen.RSAKeyGenerator;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.test.context.ActiveProfiles;

import java.time.Instant;
import java.util.Date;
import java.util.List;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;

@TestConfiguration
@ActiveProfiles("test")
public class WireMockConfig {

    private static final String REALM = "/realms/public-transport";
    private static final RSAKey SIGNING_KEY = signingKey();

    static final WireMockServer KEYCLOAK = new WireMockServer(WireMockConfiguration.options().dynamicPort());
    private static final WireMockServer GEOAPIFY = geoapifyServer();

    static {
        KEYCLOAK.start();
        stubKeycloak();
        GEOAPIFY.start();
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            KEYCLOAK.stop();
            GEOAPIFY.stop();
        }));
    }

    static String keycloakIssuer() {
        return KEYCLOAK.baseUrl() + REALM;
    }

    @Bean(destroyMethod = "")
    public WireMockServer mockGeoapifyClient() {
        return GEOAPIFY;
    }

    private static WireMockServer geoapifyServer() {
        return new WireMockServer(WireMockConfiguration.options()
                .port(8085)
                .usingFilesUnderDirectory("src/test/resources/wiremock/geoapify-client")
                .withRootDirectory("src/test/resources/wiremock/geoapify-client"));
    }

    private static void stubKeycloak() {
        KEYCLOAK.stubFor(post(urlPathEqualTo(REALM + "/protocol/openid-connect/token"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody("{\"access_token\":\"%s\",\"token_type\":\"Bearer\",\"expires_in\":86400}"
                                .formatted(accessToken()))));
        KEYCLOAK.stubFor(get(urlPathEqualTo(REALM + "/protocol/openid-connect/certs"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody(new JWKSet(SIGNING_KEY.toPublicJWK()).toString())));
    }

    private static String accessToken() {
        JWTClaimsSet claims = new JWTClaimsSet.Builder()
                .issuer(keycloakIssuer())
                .subject("pwrona")
                .claim("preferred_username", "pwrona")
                .claim("groups", List.of(
                        "/agencies/NEXTSTOP/SUPER_USER",
                        "/agencies/NEXTSTOP/AGENCY_OWNER",
                        "/agencies/NEXTSTOP/PLANNER"))
                .issueTime(Date.from(Instant.now()))
                .expirationTime(Date.from(Instant.now().plusSeconds(86_400)))
                .build();
        SignedJWT jwt = new SignedJWT(
                new JWSHeader.Builder(JWSAlgorithm.RS256).keyID(SIGNING_KEY.getKeyID()).build(),
                claims);
        try {
            jwt.sign(new RSASSASigner(SIGNING_KEY));
        } catch (JOSEException exception) {
            throw new IllegalStateException("Could not sign the test access token", exception);
        }
        return jwt.serialize();
    }

    private static RSAKey signingKey() {
        try {
            return new RSAKeyGenerator(2048)
                    .keyID("integration-test")
                    .keyUse(KeyUse.SIGNATURE)
                    .algorithm(JWSAlgorithm.RS256)
                    .generate();
        } catch (JOSEException exception) {
            throw new IllegalStateException("Could not create the test signing key", exception);
        }
    }
}
