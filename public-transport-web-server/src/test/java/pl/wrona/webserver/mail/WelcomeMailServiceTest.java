package pl.wrona.webserver.mail;

import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.thymeleaf.spring6.SpringTemplateEngine;
import org.thymeleaf.templatemode.TemplateMode;
import org.thymeleaf.templateresolver.ClassLoaderTemplateResolver;
import pl.wrona.webserver.config.AppProperties;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

class WelcomeMailServiceTest {

    private final MailService mailService = mock(MailService.class);
    private final WelcomeMailService welcomeMailService = new WelcomeMailService(
            templateEngine(),
            mailService,
            new AppProperties("http://localhost:4200/"),
            new MailProperties("mail.example.pl", 465, "smtps", "biuro@zimbra.pl", "secret", "powiadomienia@zimbra.pl", "Następna Stacja", "false"));

    @Test
    void sendsTheLoginNameAndSignInLinkWithoutThePassword() throws Exception {
        welcomeMailService.send("jan@example.com", "jan");

        ArgumentCaptor<MailMessage> message = ArgumentCaptor.forClass(MailMessage.class);
        verify(mailService).send(message.capture());
        assertThat(message.getValue().from()).isEqualTo("powiadomienia@zimbra.pl");
        assertThat(message.getValue().fromName()).isEqualTo("Następna Stacja");
        assertThat(message.getValue().to()).isEqualTo("jan@example.com");
        assertThat(message.getValue().subject()).isEqualTo("Witamy w Następna Stacja");
        assertThat(message.getValue().html()).isTrue();
        assertThat(message.getValue().body()).contains("jan");
        assertThat(message.getValue().body()).contains("http://localhost:4200/#/signin");
        assertThat(message.getValue().body()).contains("Ta wiadomość go nie zawiera");
        assertThat(message.getValue().body()).contains("cid:logo");
        assertThat(message.getValue().images()).contains(new MailMessage.InlineImage("logo", "mail/ns.png"));
    }

    private static SpringTemplateEngine templateEngine() {
        ClassLoaderTemplateResolver resolver = new ClassLoaderTemplateResolver();
        resolver.setPrefix("templates/");
        resolver.setSuffix(".html");
        resolver.setTemplateMode(TemplateMode.HTML);
        resolver.setCharacterEncoding("UTF-8");
        SpringTemplateEngine engine = new SpringTemplateEngine();
        engine.setTemplateResolver(resolver);
        return engine;
    }
}
