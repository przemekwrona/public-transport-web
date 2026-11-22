package pl.wrona.webserver.mail;

import jakarta.mail.MessagingException;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring6.SpringTemplateEngine;
import pl.wrona.webserver.config.AppProperties;

import java.util.List;
import java.util.Locale;

@Service
@AllArgsConstructor
public class WelcomeMailService {

    static final String SUBJECT = "Witamy w Następna Stacja";
    private static final String TEMPLATE = "mail/welcome";

    private final SpringTemplateEngine templateEngine;
    private final MailService mailService;
    private final AppProperties appProperties;
    private final MailProperties mailProperties;

    public void send(String email, String username) throws MessagingException {
        Context context = new Context(Locale.forLanguageTag("pl-PL"));
        context.setVariable("username", username);
        context.setVariable("loginUrl", appProperties.loginUrl());

        String body = templateEngine.process(TEMPLATE, context);
        List<MailMessage.InlineImage> images = List.of(
                new MailMessage.InlineImage("logo", "mail/ns.png"));
        MailMessage mailMessage = new MailMessage(
                mailProperties.from(), mailProperties.fromName(), email, SUBJECT, body, Boolean.TRUE, images);

        mailService.send(mailMessage);
    }
}
