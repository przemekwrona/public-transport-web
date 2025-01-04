package pl.wrona.webserver.config;

import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.UnsupportedJwtException;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;

@Slf4j
@Component
public class AuthTokenUtils {
//    private static final Logger logger = LoggerFactory.getLogger(JwtUtils.class);

//    @Value("${baeldung.app.jwtSecret}")
    private String jwtSecret = "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAzQmVOGH0XZgdcdILCCTFOCFKB83T+b5o6EaePx8XcnF8jhyNwuLGCKpII1O40NFAeDqJaRIHPEVp+sgkJs/lwoVpLXOK0rKwKCHvfs+JuI8qBzP9IkFiczkDwrUw8nBioJMAIbK+yPRgKDP9zgdyAGM7e5is1SwKfpPtsKKAJJ2LJDAe6sEJSlAMaweXCskZp2NcgjKKYVBhwl3kziLGQRbWNbxSU84SIn7kKiRM7iEs1I6gC8DZ1MtOxu8kvf7DxRca+8NJ733X9sN4lZz8B4pZpOwBl7xX607P4joLu3ye4ze9rP4fULSwD3M2wKn6wE3a8ikrRymekR9dbYRyBQIDAQAB";

//    @Value("${baeldung.app.jwtExpirationMs}")
    private int jwtExpirationMs = 36_000_000;

    public String generateJwtToken(UserDetails userDetails) {
        return Jwts.builder()
                .subject((userDetails.getUsername()))
                .issuedAt(new Date())
                .expiration(new Date((new Date()).getTime() + jwtExpirationMs))
                .signWith(getSigningKey())
                .compact();

    }

    private SecretKey getSigningKey() {
        byte[] keyBytes = Decoders.BASE64.decode(jwtSecret);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    public String getUserNameFromJwtToken(String token) {
        return Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload()
                .getSubject();

    }

    public boolean validateJwtToken(String authToken) {
        try {
            Jwts.parser()
                    .verifyWith(getSigningKey())
                    .build()
                    .parseSignedClaims(authToken);
            return true;
        } catch (MalformedJwtException e) {
            log.error("Invalid JWT token: {}", e.getMessage());
        } catch (ExpiredJwtException e) {
            log.error("JWT token is expired: {}", e.getMessage());
        } catch (UnsupportedJwtException e) {
            log.error("JWT token is unsupported: {}", e.getMessage());
        } catch (IllegalArgumentException e) {
            log.error("JWT claims string is empty: {}", e.getMessage());
        }

        return false;
    }
}
