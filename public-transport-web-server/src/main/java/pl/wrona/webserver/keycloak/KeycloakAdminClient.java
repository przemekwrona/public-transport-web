package pl.wrona.webserver.keycloak;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
@ConditionalOnProperty(prefix = "keycloak", name = "enabled", havingValue = "true", matchIfMissing = true)
public class KeycloakAdminClient {

    private static final Pattern BCRYPT_ITERATIONS = Pattern.compile("\\$2[aby]\\$(\\d+)\\$");

    private final RestClient restClient;
    private final KeycloakProperties properties;
    private final ObjectMapper objectMapper;

    private String accessToken;
    private Instant accessTokenExpiresAt = Instant.EPOCH;

    @Autowired
    public KeycloakAdminClient(KeycloakProperties properties, ObjectMapper objectMapper) {
        this(RestClient.builder().baseUrl(properties.getServerUrl()).build(), properties, objectMapper);
    }

    KeycloakAdminClient(RestClient restClient, KeycloakProperties properties, ObjectMapper objectMapper) {
        this.restClient = restClient;
        this.properties = properties;
        this.objectMapper = objectMapper;
    }

    public String ensureGroup(String parentId, String name, Map<String, List<String>> attributes) {
        JsonNode existing = findGroup(parentId, name);
        if (existing != null) {
            return existing.path("id").asText();
        }

        ObjectNode body = objectMapper.createObjectNode();
        body.put("name", name);
        if (attributes != null && !attributes.isEmpty()) {
            body.set("attributes", objectMapper.valueToTree(attributes));
        }

        Map<String, String> uriVariables = parentId == null
                ? Map.of("realm", properties.getRealm())
                : Map.of("realm", properties.getRealm(), "parentId", parentId);
        String uri = parentId == null
                ? "/admin/realms/{realm}/groups"
                : "/admin/realms/{realm}/groups/{parentId}/children";
        try {
            String groupId = restClient.post()
                    .uri(uri, uriVariables)
                    .headers(headers -> headers.setBearerAuth(token()))
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(body)
                    .exchange((request, response) -> {
                        if (response.getStatusCode().value() == 409) {
                            return null;
                        }
                        if (response.getStatusCode().isError()) {
                            throw adminFailure(response.getStatusCode().value(), new String(response.getBody().readAllBytes()));
                        }
                        String location = response.getHeaders().getLocation() == null ? "" : response.getHeaders().getLocation().toString();
                        int slash = location.lastIndexOf('/');
                        if (slash < 0 || slash == location.length() - 1) {
                            throw new IllegalStateException("Keycloak did not return a group id for " + name);
                        }
                        return location.substring(slash + 1);
                    });
            if (groupId != null) {
                return groupId;
            }
            JsonNode created = findGroup(parentId, name);
            if (created == null) {
                throw new IllegalStateException("Keycloak group already exists but was not found: " + name);
            }
            return created.path("id").asText();
        } catch (RestClientResponseException exception) {
            throw adminFailure(exception);
        }
    }

    public void assignClientRole(String groupId, String roleName) {
        String clientUuid = clientUuid(properties.getWebClientId());
        JsonNode role = clientRole(clientUuid, roleName);
        try {
            restClient.post()
                    .uri("/admin/realms/{realm}/groups/{groupId}/role-mappings/clients/{clientUuid}",
                            properties.getRealm(), groupId, clientUuid)
                    .headers(headers -> headers.setBearerAuth(token()))
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(List.of(Map.of("id", role.path("id").asText(), "name", role.path("name").asText())))
                    .retrieve()
                    .toBodilessEntity();
        } catch (RestClientResponseException exception) {
            throw adminFailure(exception);
        }
    }

    public String ensureUser(String username, String email, String bcryptPassword) {
        JsonNode existing = userByUsername(username);
        if (existing != null) {
            return existing.path("id").asText();
        }

        ObjectNode body = objectMapper.createObjectNode();
        body.put("username", username);
        body.put("enabled", true);
        body.put("firstName", username);
        body.put("lastName", username);
        if (StringUtils.hasText(email)) {
            body.put("email", email);
            body.put("emailVerified", true);
        }
        if (StringUtils.hasText(bcryptPassword)) {
            body.set("credentials", objectMapper.valueToTree(List.of(bcryptCredential(bcryptPassword))));
        }

        try {
            String userId = restClient.post()
                    .uri("/admin/realms/{realm}/users", properties.getRealm())
                    .headers(headers -> headers.setBearerAuth(token()))
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(body)
                    .exchange((request, response) -> {
                        if (response.getStatusCode().value() == 409) {
                            return null;
                        }
                        if (response.getStatusCode().isError()) {
                            throw adminFailure(response.getStatusCode().value(), new String(response.getBody().readAllBytes()));
                        }
                        String location = response.getHeaders().getLocation() == null ? "" : response.getHeaders().getLocation().toString();
                        int slash = location.lastIndexOf('/');
                        if (slash < 0 || slash == location.length() - 1) {
                            throw new IllegalStateException("Keycloak did not return a user id for " + username);
                        }
                        return location.substring(slash + 1);
                    });
            if (userId == null) {
                JsonNode user = userByUsername(username);
                if (user == null) {
                    throw new IllegalStateException("Keycloak user already exists but was not found: " + username);
                }
                return user.path("id").asText();
            }
            return userId;
        } catch (RestClientResponseException exception) {
            throw adminFailure(exception);
        }
    }

    public void ensureLoginClient(String clientId, String clientSecret) {
        try {
            String clientUuid = clientUuidOrNull(clientId);
            if (clientUuid == null) {
                clientUuid = createLoginClient(clientId, clientSecret);
            }
            ensureGroupsMapper(clientUuid);
        } catch (RestClientResponseException exception) {
            throw adminFailure(exception);
        }
    }

    private String clientUuidOrNull(String clientId) {
        JsonNode clients = restClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/admin/realms/{realm}/clients")
                        .queryParam("clientId", clientId)
                        .build(properties.getRealm()))
                .headers(headers -> headers.setBearerAuth(token()))
                .retrieve()
                .body(JsonNode.class);
        if (clients == null || !clients.isArray() || clients.isEmpty()) {
            return null;
        }
        return clients.get(0).path("id").asText();
    }

    private String createLoginClient(String clientId, String clientSecret) {
        ObjectNode body = objectMapper.createObjectNode();
        body.put("clientId", clientId);
        body.put("name", "Public Transport Login");
        body.put("protocol", "openid-connect");
        body.put("publicClient", false);
        body.put("secret", clientSecret);
        body.put("standardFlowEnabled", false);
        body.put("implicitFlowEnabled", false);
        body.put("directAccessGrantsEnabled", true);
        body.put("serviceAccountsEnabled", false);
        body.put("fullScopeAllowed", true);
        String location = restClient.post()
                .uri("/admin/realms/{realm}/clients", properties.getRealm())
                .headers(headers -> headers.setBearerAuth(token()))
                .contentType(MediaType.APPLICATION_JSON)
                .body(body)
                .exchange((request, response) -> {
                    if (response.getStatusCode().isError()) {
                        throw adminFailure(response.getStatusCode().value(), new String(response.getBody().readAllBytes()));
                    }
                    return response.getHeaders().getLocation() == null ? "" : response.getHeaders().getLocation().toString();
                });
        int slash = location.lastIndexOf('/');
        if (slash >= 0 && slash < location.length() - 1) {
            return location.substring(slash + 1);
        }
        String clientUuid = clientUuidOrNull(clientId);
        if (clientUuid == null) {
            throw new IllegalStateException("Keycloak did not return a client id for " + clientId);
        }
        return clientUuid;
    }

    private void ensureGroupsMapper(String clientUuid) {
        JsonNode mappers = restClient.get()
                .uri("/admin/realms/{realm}/clients/{clientUuid}/protocol-mappers/models", properties.getRealm(), clientUuid)
                .headers(headers -> headers.setBearerAuth(token()))
                .retrieve()
                .body(JsonNode.class);
        if (mappers != null && mappers.isArray()) {
            for (JsonNode mapper : mappers) {
                if ("groups".equals(mapper.path("name").asText())) {
                    return;
                }
            }
        }
        ObjectNode config = objectMapper.createObjectNode();
        config.put("full.path", "true");
        config.put("introspection.token.claim", "true");
        config.put("userinfo.token.claim", "true");
        config.put("multivalued", "true");
        config.put("id.token.claim", "true");
        config.put("access.token.claim", "true");
        config.put("claim.name", "groups");
        ObjectNode body = objectMapper.createObjectNode();
        body.put("name", "groups");
        body.put("protocol", "openid-connect");
        body.put("protocolMapper", "oidc-group-membership-mapper");
        body.set("config", config);
        restClient.post()
                .uri("/admin/realms/{realm}/clients/{clientUuid}/protocol-mappers/models", properties.getRealm(), clientUuid)
                .headers(headers -> headers.setBearerAuth(token()))
                .contentType(MediaType.APPLICATION_JSON)
                .body(body)
                .retrieve()
                .toBodilessEntity();
    }

    public boolean fillMissingProfile(String username) {
        JsonNode user = userByUsername(username);
        if (!(user instanceof ObjectNode body)) {
            return false;
        }
        boolean missingFirstName = !StringUtils.hasText(body.path("firstName").asText());
        boolean missingLastName = !StringUtils.hasText(body.path("lastName").asText());
        if (!missingFirstName && !missingLastName) {
            return false;
        }
        if (missingFirstName) {
            body.put("firstName", username);
        }
        if (missingLastName) {
            body.put("lastName", username);
        }
        try {
            restClient.put()
                    .uri("/admin/realms/{realm}/users/{userId}", properties.getRealm(), body.path("id").asText())
                    .headers(headers -> headers.setBearerAuth(token()))
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(body)
                    .retrieve()
                    .toBodilessEntity();
            return true;
        } catch (RestClientResponseException exception) {
            throw adminFailure(exception);
        }
    }

    public void addUserToGroup(String userId, String groupId) {
        try {
            restClient.put()
                    .uri("/admin/realms/{realm}/users/{userId}/groups/{groupId}", properties.getRealm(), userId, groupId)
                    .headers(headers -> headers.setBearerAuth(token()))
                    .retrieve()
                    .toBodilessEntity();
        } catch (RestClientResponseException exception) {
            throw adminFailure(exception);
        }
    }

    Map<String, Object> bcryptCredential(String hash) {
        int iterations = 10;
        Matcher matcher = BCRYPT_ITERATIONS.matcher(hash);
        if (matcher.find()) {
            iterations = Integer.parseInt(matcher.group(1));
        }
        Map<String, Object> credential = new LinkedHashMap<>();
        credential.put("type", "password");
        credential.put("temporary", false);
        credential.put("credentialData", "{\"hashIterations\":" + iterations + ",\"algorithm\":\"bcrypt\",\"additionalParameters\":{}}");
        credential.put("secretData", "{\"value\":\"" + hash + "\",\"salt\":\"\"}");
        return credential;
    }

    private JsonNode findGroup(String parentId, String name) {
        try {
            JsonNode groups = parentId == null
                    ? restClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .path("/admin/realms/{realm}/groups")
                            .queryParam("search", name)
                            .build(properties.getRealm()))
                    .headers(headers -> headers.setBearerAuth(token()))
                    .retrieve()
                    .body(JsonNode.class)
                    : restClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .path("/admin/realms/{realm}/groups/{parentId}/children")
                            .queryParam("search", name)
                            .build(properties.getRealm(), parentId))
                    .headers(headers -> headers.setBearerAuth(token()))
                    .retrieve()
                    .body(JsonNode.class);
            if (groups == null || !groups.isArray()) {
                return null;
            }
            for (JsonNode group : groups) {
                if (name.equals(group.path("name").asText())) {
                    return group;
                }
            }
            return null;
        } catch (RestClientResponseException exception) {
            throw adminFailure(exception);
        }
    }

    private JsonNode userByUsername(String username) {
        try {
            JsonNode users = restClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .path("/admin/realms/{realm}/users")
                            .queryParam("username", username)
                            .queryParam("exact", true)
                            .build(properties.getRealm()))
                    .headers(headers -> headers.setBearerAuth(token()))
                    .retrieve()
                    .body(JsonNode.class);
            if (users == null || !users.isArray() || users.isEmpty()) {
                return null;
            }
            return users.get(0);
        } catch (RestClientResponseException exception) {
            throw adminFailure(exception);
        }
    }

    private String clientUuid(String clientId) {
        try {
            JsonNode clients = restClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .path("/admin/realms/{realm}/clients")
                            .queryParam("clientId", clientId)
                            .build(properties.getRealm()))
                    .headers(headers -> headers.setBearerAuth(token()))
                    .retrieve()
                    .body(JsonNode.class);
            if (clients == null || !clients.isArray() || clients.isEmpty()) {
                throw new IllegalStateException("Keycloak client was not found: " + clientId);
            }
            return clients.get(0).path("id").asText();
        } catch (RestClientResponseException exception) {
            throw adminFailure(exception);
        }
    }

    private JsonNode clientRole(String clientUuid, String roleName) {
        try {
            JsonNode role = restClient.get()
                    .uri("/admin/realms/{realm}/clients/{clientUuid}/roles/{roleName}", properties.getRealm(), clientUuid, roleName)
                    .headers(headers -> headers.setBearerAuth(token()))
                    .retrieve()
                    .body(JsonNode.class);
            if (role == null || !role.hasNonNull("id")) {
                throw new IllegalStateException("Keycloak client role was not found: " + roleName);
            }
            return role;
        } catch (RestClientResponseException exception) {
            throw adminFailure(exception);
        }
    }

    private synchronized String token() {
        if (accessToken != null && Instant.now().isBefore(accessTokenExpiresAt)) {
            return accessToken;
        }
        LinkedMultiValueMap<String, String> form = new LinkedMultiValueMap<>();
        form.add("grant_type", "client_credentials");
        form.add("client_id", properties.getProvisioner().getClientId());
        form.add("client_secret", properties.getProvisioner().getClientSecret());
        try {
            JsonNode token = restClient.post()
                    .uri("/realms/{realm}/protocol/openid-connect/token", properties.getRealm())
                    .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                    .body(form)
                    .retrieve()
                    .body(JsonNode.class);
            if (token == null || !token.hasNonNull("access_token")) {
                throw new IllegalStateException("Keycloak did not return an access token");
            }
            accessToken = token.path("access_token").asText();
            long expiresIn = token.path("expires_in").asLong(60);
            accessTokenExpiresAt = Instant.now().plusSeconds(Math.max(1, expiresIn - 15));
            return accessToken;
        } catch (RestClientResponseException exception) {
            throw adminFailure(exception);
        }
    }

    private IllegalStateException adminFailure(RestClientResponseException exception) {
        return adminFailure(exception.getStatusCode().value(), exception.getResponseBodyAsString());
    }

    private IllegalStateException adminFailure(int status, String body) {
        return new IllegalStateException("Keycloak admin call failed: " + status + " " + body);
    }
}
