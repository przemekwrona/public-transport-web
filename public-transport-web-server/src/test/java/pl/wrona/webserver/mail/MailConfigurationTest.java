package pl.wrona.webserver.mail;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(classes = MailConfiguration.class)
@ActiveProfiles("test")
class MailConfigurationTest {

    @Autowired
    private JavaMailSenderImpl mailSender;

    @Test
    void configuresTheZimbraMailbox() {
        assertThat(mailSender.getHost()).isEqualTo("mail.example.pl");
        assertThat(mailSender.getPort()).isEqualTo(465);
        assertThat(mailSender.getProtocol()).isEqualTo("smtps");
        assertThat(mailSender.getUsername()).isEqualTo("biuro@zimbra.pl");
        assertThat(mailSender.getPassword()).isEqualTo("zimbra-secret");
        assertThat(mailSender.getJavaMailProperties().getProperty("mail.smtps.ssl.enable")).isEqualTo("true");
        assertThat(mailSender.getJavaMailProperties().getProperty("mail.smtps.ssl.trust")).isEqualTo("mail.example.pl");
    }
}
