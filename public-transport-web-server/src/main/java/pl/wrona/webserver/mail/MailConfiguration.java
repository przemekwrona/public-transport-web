package pl.wrona.webserver.mail;

import org.jspecify.annotations.NonNull;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.mail.javamail.JavaMailSenderImpl;

import java.nio.charset.StandardCharsets;
import java.util.Properties;

@Configuration
@EnableConfigurationProperties(MailProperties.class)
class MailConfiguration {

    @Bean
    JavaMailSenderImpl mailSender(MailProperties properties) {
        JavaMailSenderImpl sender = new JavaMailSenderImpl();
        sender.setHost(properties.host());
        sender.setPort(properties.port());
        sender.setUsername(properties.username());
        sender.setPassword(properties.password());
        sender.setDefaultEncoding(StandardCharsets.UTF_8.name());
        sender.setProtocol(properties.protocol());

        Properties mailProperties = smtpProperties(properties);
        sender.setJavaMailProperties(mailProperties);
        return sender;
    }

    private static @NonNull Properties smtpProperties(MailProperties properties) {
        Properties mailProperties = new Properties();
        mailProperties.put("mail.smtp.auth", "true");
        mailProperties.put("mail.smtp.connectiontimeout", "15000");
        mailProperties.put("mail.smtp.timeout", "20000");
        mailProperties.put("mail.smtp.writetimeout", "20000");
        mailProperties.put("mail.smtp.ssl.protocols", "TLSv1.2");
        mailProperties.put("mail.smtp.starttls.enable", "true");
        mailProperties.put("mail.smtp.starttls.required", "true");
        if (properties.trustServerCertificate() && properties.host() != null && !properties.host().isBlank()) {
            mailProperties.put("mail.smtp.ssl.trust", properties.host());
        }
        return mailProperties;
    }
}
