package pl.wrona.webserver.mail;

import java.util.List;

public record MailMessage(String from, String fromName, String to, String subject, String body, boolean html, List<InlineImage> images) {

    public record InlineImage(String contentId, String classpathLocation) {
    }
}
