package pl.wrona.webserver.security;

import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;

import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.regex.Pattern;

/**
 * Roles and agencies taken from the Keycloak {@code groups} claim.
 * A group path is {@code /agencies/{agencyCode}/{role}}.
 */
public final class AgencyTokenPrincipal {

    private static final Pattern AGENCY_CODE = Pattern.compile("[A-Za-z0-9][A-Za-z0-9_-]{0,63}");
    private static final String AGENCY_OWNER = PreAgencyAuthorize.Role.AGENCY_OWNER.name();

    private final String username;
    private final Map<String, Set<String>> rolesByAgency;

    private AgencyTokenPrincipal(String username, Map<String, Set<String>> rolesByAgency) {
        this.username = username;
        this.rolesByAgency = rolesByAgency;
    }

    public static AgencyTokenPrincipal from(Jwt jwt) {
        String username = jwt.getClaimAsString("preferred_username");
        if (username == null || username.isBlank()) {
            username = jwt.getSubject();
        }
        if (username == null || username.isBlank()) {
            throw new BadCredentialsException("Token has no username");
        }
        return new AgencyTokenPrincipal(username, parseGroups(groupClaims(jwt)));
    }

    public String username() {
        return username;
    }

    public boolean hasAgency(String agencyCode) {
        return agencyCode != null && rolesByAgency.containsKey(agencyCode);
    }

    public boolean hasRole(String agencyCode, String role) {
        if (agencyCode == null || role == null) {
            return false;
        }
        return rolesByAgency.getOrDefault(agencyCode, Set.of()).stream()
                .anyMatch(role::equalsIgnoreCase);
    }

    public List<String> roles() {
        return rolesByAgency.values().stream()
                .flatMap(Set::stream)
                .distinct()
                .sorted()
                .toList();
    }

    public Collection<? extends GrantedAuthority> authorities() {
        return roles().stream().map(SimpleGrantedAuthority::new).toList();
    }

    /**
     * Agency this login session starts in. An owned agency wins. Otherwise the first code in alphabetical order.
     */
    public Optional<String> primaryAgencyCode() {
        Optional<String> owned = rolesByAgency.entrySet().stream()
                .filter(entry -> entry.getValue().contains(AGENCY_OWNER))
                .map(Map.Entry::getKey)
                .min(Comparator.naturalOrder());
        if (owned.isPresent()) {
            return owned;
        }
        return rolesByAgency.keySet().stream().min(Comparator.naturalOrder());
    }

    private static List<String> groupClaims(Jwt jwt) {
        Object claim = jwt.getClaim("groups");
        if (claim instanceof String group) {
            return List.of(group);
        }
        if (claim instanceof Collection<?> groups) {
            return groups.stream()
                    .filter(String.class::isInstance)
                    .map(String.class::cast)
                    .toList();
        }
        return List.of();
    }

    private static Map<String, Set<String>> parseGroups(List<String> groups) {
        Map<String, Set<String>> rolesByAgency = new TreeMap<>();
        for (String group : groups) {
            if (group == null || group.isBlank()) {
                continue;
            }
            String[] parts = group.split("/", -1);
            if ((parts.length != 3 && parts.length != 4)
                    || !parts[0].isEmpty()
                    || !"agencies".equals(parts[1])
                    || !AGENCY_CODE.matcher(parts[2]).matches()) {
                continue;
            }
            Set<String> roles = rolesByAgency.computeIfAbsent(parts[2], ignored -> new TreeSet<>());
            if (parts.length == 4) {
                canonicalRole(parts[3]).ifPresent(roles::add);
            }
        }
        Map<String, Set<String>> immutable = new TreeMap<>();
        rolesByAgency.forEach((agency, roles) -> immutable.put(agency, Set.copyOf(roles)));
        return Map.copyOf(immutable);
    }

    private static Optional<String> canonicalRole(String raw) {
        for (PreAgencyAuthorize.Role role : PreAgencyAuthorize.Role.values()) {
            if (role.name().equalsIgnoreCase(raw)) {
                return Optional.of(role.name());
            }
        }
        return Optional.empty();
    }

}
