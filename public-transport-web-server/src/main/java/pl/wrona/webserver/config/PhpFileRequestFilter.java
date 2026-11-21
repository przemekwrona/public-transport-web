package pl.wrona.webserver.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class PhpFileRequestFilter extends OncePerRequestFilter {

    /**
     * Scanner probes for software this service does not run. Matched as path fragments so nested
     * WordPress directories ({@code /wp-admin/css/}, {@code /wp-content/uploads/...}) are covered
     * without listing each one.
     */
    private static final List<String> BLACKLISTED_FRAGMENTS = List.of(
            ".php",
            "/wordpress",
            "/wp-admin",
            "/wp-content",
            "/wp-includes",
            "/license.txt"
    );

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        if (requestsBlacklistedPath(request)) {
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            return;
        }
        filterChain.doFilter(request, response);
    }

    private static boolean requestsBlacklistedPath(HttpServletRequest request) {
        String uri = request.getRequestURI();
        if (uri == null) {
            return false;
        }
        String path = uri.toLowerCase(Locale.ROOT);
        for (String fragment : BLACKLISTED_FRAGMENTS) {
            if (path.contains(fragment)) {
                return true;
            }
        }
        return false;
    }

}
