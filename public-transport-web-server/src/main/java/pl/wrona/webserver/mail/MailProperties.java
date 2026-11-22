package pl.wrona.webserver.mail;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "mail")
public record MailProperties(
        String host,
        int port,
        String protocol,
        String username,
        String password,
        String from,
        String fromName,
        boolean trustServerCertificate
) {
}
