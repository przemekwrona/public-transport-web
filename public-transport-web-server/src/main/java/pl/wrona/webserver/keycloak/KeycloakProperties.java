package pl.wrona.webserver.keycloak;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties(prefix = "keycloak")
public class KeycloakProperties {

    private boolean enabled = true;
    private String serverUrl = "http://localhost:8081";
    private String realm = "public-transport";
    private String issuerUri = "http://localhost:8081/realms/public-transport";
    private String jwkSetUri = "http://localhost:8081/realms/public-transport/protocol/openid-connect/certs";
    private String webClientId = "public-transport-web";
    private Provisioner provisioner = new Provisioner();
    private Login login = new Login();

    @Data
    public static class Login {
        private String clientId = "public-transport-login";
        private String clientSecret = "login-dev-secret";
    }

    @Data
    public static class Provisioner {
        private String clientId = "public-transport-provisioner";
        private String clientSecret = "provisioner-dev-secret";
    }
}
