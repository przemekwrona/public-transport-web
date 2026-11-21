package pl.wrona.webserver.mail;

import jakarta.mail.Session;
import jakarta.mail.internet.MimeMessage;
import jakarta.mail.internet.MimeMultipart;
import org.junit.jupiter.api.Test;
import org.springframework.mail.javamail.JavaMailSender;

import java.util.List;
import java.util.Properties;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class MailServiceTest {

    private final JavaMailSender sender = mock(JavaMailSender.class);
    private final MailService mailService = new MailService(sender);

    @Test
    void sendsTheMessage() throws Exception {
        MimeMessage message = mimeMessage();
        when(sender.createMimeMessage()).thenReturn(message);

        mailService.send(message("powiadomienia@zimbra.pl", "jan@example.com", "Temat", "Treść", false, List.of()));

        verify(sender).send(message);
        assertThat(message.getSubject()).isEqualTo("Temat");
        assertThat(message.getFrom()[0].toString()).isEqualTo("powiadomienia@zimbra.pl");
        assertThat(message.getAllRecipients()[0].toString()).isEqualTo("jan@example.com");
    }

    @Test
    void sendsHtml() throws Exception {
        MimeMessage message = mimeMessage();
        when(sender.createMimeMessage()).thenReturn(message);

        mailService.send(message("biuro@zimbra.pl", "jan@example.com", "Temat", "<p>Treść</p>", true, List.of()));

        verify(sender).send(message);
        assertThat(message.getDataHandler().getContentType()).contains("text/html");
    }

    @Test
    void embedsTheLogoInTheMessage() throws Exception {
        MimeMessage message = mimeMessage();
        when(sender.createMimeMessage()).thenReturn(message);

        mailService.send(message(
                "biuro@zimbra.pl",
                "jan@example.com",
                "Temat",
                "<img src=\"cid:logo\" alt=\"Następna Stacja\"/>",
                true,
                List.of(new MailMessage.InlineImage("logo", "mail/ns.png"))));

        message.saveChanges();
        MimeMultipart multipart = (MimeMultipart) message.getContent();
        assertThat(multipart.getContentType()).contains("multipart/related");
        assertThat(multipart.getBodyPart(1).getContentType()).contains("image/png");
        assertThat(multipart.getBodyPart(1).getHeader("Content-ID")[0]).contains("logo");
    }

    private static MailMessage message(String from, String to, String subject, String body, boolean html, List<MailMessage.InlineImage> images) {
        return new MailMessage(from, to, subject, body, html, images);
    }

    private static MimeMessage mimeMessage() {
        return new MimeMessage(Session.getInstance(new Properties()));
    }
}
