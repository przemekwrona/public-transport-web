package pl.wrona.webserver.mail;

public class MailDeliveryException extends RuntimeException {

    public MailDeliveryException(String recipient, Throwable cause) {
        super("Could not send mail to " + recipient, cause);
    }
}
