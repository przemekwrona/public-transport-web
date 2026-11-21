package pl.wrona.webserver.mail;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.AllArgsConstructor;
import org.springframework.core.io.ClassPathResource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;

@Service
@AllArgsConstructor
public class MailService {

    private final JavaMailSender sender;

    public void send(MailMessage message) throws MessagingException {
        MimeMessage mimeMessage = sender.createMimeMessage();
        int multipartMode = message.images().isEmpty()
                ? MimeMessageHelper.MULTIPART_MODE_NO
                : MimeMessageHelper.MULTIPART_MODE_RELATED;
        MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, multipartMode, StandardCharsets.UTF_8.name());
        helper.setFrom(message.from());
        helper.setTo(message.to());
        helper.setSubject(message.subject());
        helper.setText(message.body(), message.html());
        for (MailMessage.InlineImage image : message.images()) {
            helper.addInline(image.contentId(), new ClassPathResource(image.classpathLocation()));
        }
        sender.send(mimeMessage);
    }

}
