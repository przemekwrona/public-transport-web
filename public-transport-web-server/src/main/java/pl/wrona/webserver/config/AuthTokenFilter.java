package pl.wrona.webserver.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtException;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;
import pl.wrona.webserver.security.AppUserDetailsService;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Collection;
import java.util.List;
import java.util.Map;

@Component
@AllArgsConstructor
public class AuthTokenFilter extends OncePerRequestFilter {

    private final AppUserDetailsService appUserDetailsService;
    private final AuthTokenUtils authTokenUtils;
    private final ObjectProvider<JwtDecoder> keycloakJwtDecoder;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        try {
            String jwt = parseJwt(request);
            if (jwt != null && !authenticate(request, jwt)) {
                logger.debug("Rejected bearer token");
            }
        } catch (Exception e) {
            logger.error("Cannot set user authentication: {}", e);
        }

        filterChain.doFilter(request, response);
    }

    private boolean authenticate(HttpServletRequest request, String jwt) {
        if (isKeycloakToken(jwt)) {
            return authenticateWithKeycloak(request, jwt);
        }
        if (!authTokenUtils.validateJwtToken(jwt)) {
            return false;
        }
        authenticateUser(request, appUserDetailsService.loadUserByUsername(authTokenUtils.getUserNameFromJwtToken(jwt)), List.of());
        return true;
    }

    private List<GrantedAuthority> keycloakAuthorities(Jwt token) {
        List<GrantedAuthority> authorities = new ArrayList<>();
        Object realmAccess = token.getClaim("realm_access");
        if (realmAccess instanceof Map<?, ?> access && access.get("roles") instanceof Collection<?> roles && roles.contains("super_user")) {
            authorities.add(new SimpleGrantedAuthority("super_user"));
        }
        Object groups = token.getClaim("groups");
        if (groups instanceof Collection<?> groupPaths) {
            for (Object group : groupPaths) {
                if (group instanceof String path && path.startsWith("/agencies/")) {
                    authorities.add(new SimpleGrantedAuthority("GROUP_" + path));
                }
            }
        }
        return authorities;
    }

    private boolean authenticateWithKeycloak(HttpServletRequest request, String jwt) {
        JwtDecoder decoder = keycloakJwtDecoder.getIfAvailable();
        if (decoder == null) {
            return false;
        }
        try {
            Jwt token = decoder.decode(jwt);
            String username = token.getClaimAsString("preferred_username");
            if (!StringUtils.hasText(username)) {
                username = token.getSubject();
            }
            authenticateUser(request, appUserDetailsService.loadUserByUsername(username), keycloakAuthorities(token));
            return true;
        } catch (JwtException | UsernameNotFoundException exception) {
            logger.debug("Keycloak token rejected: " + exception.getMessage());
            return false;
        }
    }

    private void authenticateUser(HttpServletRequest request, UserDetails userDetails, Collection<? extends GrantedAuthority> tokenAuthorities) {
        List<GrantedAuthority> authorities = new ArrayList<>(userDetails.getAuthorities());
        for (GrantedAuthority authority : tokenAuthorities) {
            boolean alreadyPresent = authorities.stream().anyMatch(existing -> existing.getAuthority().equals(authority.getAuthority()));
            if (!alreadyPresent) {
                authorities.add(authority);
            }
        }
        UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(userDetails, null, authorities);
        authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
        SecurityContextHolder.getContext().setAuthentication(authentication);
    }

    private String padBase64(String value) {
        int padding = (4 - value.length() % 4) % 4;
        return value + "=".repeat(padding);
    }

    private boolean isKeycloakToken(String jwt) {
        int separator = jwt.indexOf('.');
        if (separator <= 0) {
            return false;
        }
        try {
            String header = new String(Base64.getUrlDecoder().decode(padBase64(jwt.substring(0, separator))));
            return header.contains("\"RS256\"");
        } catch (IllegalArgumentException exception) {
            return false;
        }
    }

    private String parseJwt(HttpServletRequest request) {
        String headerAuth = request.getHeader("Authorization");

        if (StringUtils.hasText(headerAuth) && headerAuth.startsWith("Bearer ")) {
            return headerAuth.substring(7);
        }

        return null;
    }
}
