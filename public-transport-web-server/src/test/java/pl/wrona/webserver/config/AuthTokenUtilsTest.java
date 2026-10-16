package pl.wrona.webserver.config;

import org.junit.jupiter.api.Test;
import org.springframework.security.core.userdetails.User;

import static org.assertj.core.api.Assertions.assertThat;

class AuthTokenUtilsTest {

    @Test
    void encodesATokenThatDecodesToTheSameSubject() {
        AuthTokenUtils tokens = new AuthTokenUtils();
        User user = (User) User.withUsername("pwrona").password("secret").roles("USER").build();

        String token = tokens.generateJwtToken(user);

        assertThat(tokens.validateJwtToken(token)).isTrue();
        assertThat(tokens.getUserNameFromJwtToken(token)).isEqualTo("pwrona");
        assertThat(tokens.validateJwtToken("not-a-jwt")).isFalse();
    }

}
