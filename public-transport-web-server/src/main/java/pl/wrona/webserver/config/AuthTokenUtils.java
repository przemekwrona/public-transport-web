package pl.wrona.webserver.config;

import com.nimbusds.jose.jwk.source.ImmutableSecret;
import com.nimbusds.jose.proc.SecurityContext;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.security.oauth2.jwt.JwtException;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.time.Instant;
import java.util.Base64;

@Slf4j
@Component
public class AuthTokenUtils {

//    @Value("${baeldung.app.jwtSecret}")
    private String jwtSecret = "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAzQmVOGH0XZgdcdILCCTFOCFKB83T+b5o6EaePx8XcnF8jhyNwuLGCKpII1O40NFAeDqJaRIHPEVp+sgkJs/lwoVpLXOK0rKwKCHvfs+JuI8qBzP9IkFiczkDwrUw8nBioJMAIbK+yPRgKDP9zgdyAGM7e5is1SwKfpPtsKKAJJ2LJDAe6sEJSlAMaweXCskZp2NcgjKKYVBhwl3kziLGQRbWNbxSU84SIn7kKiRM7iEs1I6gC8DZ1MtOxu8kvf7DxRca+8NJ733X9sN4lZz8B4pZpOwBl7xX607P4joLu3ye4ze9rP4fULSwD3M2wKn6wE3a8ikrRymekR9dbYRyBQIDAQAB";

//    @Value("${baeldung.app.jwtExpirationMs}")
    private int jwtExpirationMs = 36_000_000;

    private final JwtEncoder jwtEncoder;
    private final JwtDecoder jwtDecoder;

    public AuthTokenUtils() {
        byte[] keyBytes = Base64.getDecoder().decode(jwtSecret);
        SecretKey secretKey = new SecretKeySpec(keyBytes, "HmacSHA512");
        this.jwtEncoder = new NimbusJwtEncoder(new ImmutableSecret<SecurityContext>(secretKey));
        this.jwtDecoder = NimbusJwtDecoder.withSecretKey(secretKey)
                .macAlgorithm(MacAlgorithm.HS512)
                .build();
    }

    public String generateJwtToken(UserDetails userDetails) {
        Instant issuedAt = Instant.now();
        JwtClaimsSet claims = JwtClaimsSet.builder()
                .subject(userDetails.getUsername())
                .issuedAt(issuedAt)
                .expiresAt(issuedAt.plusMillis(jwtExpirationMs))
                .build();
        JwsHeader header = JwsHeader.with(MacAlgorithm.HS512).build();
        return jwtEncoder.encode(JwtEncoderParameters.from(header, claims)).getTokenValue();
    }

    public String getUserNameFromJwtToken(String token) {
        return jwtDecoder.decode(token).getSubject();
    }

    public boolean validateJwtToken(String authToken) {
        try {
            jwtDecoder.decode(authToken);
            return true;
        } catch (JwtException exception) {
            log.error("Invalid JWT token: {}", exception.getMessage());
        }
        return false;
    }
}
