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
        sender.setJavaMailProperties(smtpsProperties(properties));
        return sender;
    }

    private static @NonNull Properties smtpsProperties(MailProperties properties) {
        Properties mailProperties = new Properties();
        mailProperties.put("mail.smtps.auth", "true");
        mailProperties.put("mail.smtps.ssl.enable", "true");
        mailProperties.put("mail.smtps.connectiontimeout", "15000");
        mailProperties.put("mail.smtps.timeout", "30000");
        mailProperties.put("mail.smtps.writetimeout", "30000");
        mailProperties.put("mail.smtps.ssl.protocols", "TLSv1.2");
        if (properties.host() != null) {
            mailProperties.put("mail.smtps.ssl.trust", properties.host());
        }

        return mailProperties;
    }
}
