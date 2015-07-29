import org.jsoup.Jsoup;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import java.util.Properties;

public class Malvik {

    private static final String BASE_URI = "https://www.malvik.cz";
    private static final String RESOURCE_URI = "/Tema/Aktuality-z-Malvika/";
    private static final String URI = BASE_URI + RESOURCE_URI;

    private static final long HOURS = 1000 * 60 * 60 * 4;

    private static final String SMTP_HOST_NAME = "smtp.gmail.com";
    private static final String SMTP_AUTH_USER = System.getenv("GMAIL_ADDRESS");
    private static final String SMTP_AUTH_PWD = System.getenv("GMAIL_PASSWORD");

    private static final Properties properties = new Properties();

    static {
        properties.put("mail.transport.protocol", "smtp");
        properties.put("mail.smtp.host", SMTP_HOST_NAME);
        properties.put("mail.smtp.port", 587);
        properties.put("mail.smtp.auth", "true");
        properties.put("mail.smtp.starttls.enable", "true");
    }

    private static final Session session = Session.getInstance(properties,
            new javax.mail.Authenticator() {
                protected PasswordAuthentication getPasswordAuthentication() {
                    return new PasswordAuthentication(SMTP_AUTH_USER, SMTP_AUTH_PWD);
                }
            });

    private static void email(final String offers) {
        try {

            final MimeMessage message = new MimeMessage(session);
            final Multipart multipart = new MimeMultipart();
            final BodyPart body = new MimeBodyPart();
            body.setContent("<html>" +
                            "<meta charset=\"UTF-8\"></meta>" +
                            "<body>" + offers + "</body>" +
                            "</html>",
                    "text/html; charset=utf-8");
            multipart.addBodyPart(body);
            message.setFrom(new InternetAddress(System.getenv("GMAIL_ADDRESS")));
            message.addRecipients(Message.RecipientType.TO, new Address[] {
                    new InternetAddress("XX"),
                    new InternetAddress("XX")
            });
            message.setSubject("Malvik sale!");
            message.setContent(multipart);
            Transport.send(message);
            System.out.println("An e-mail containing the new offers has been sent.");
        } catch (final MessagingException e) {
            e.printStackTrace(System.err);
        }
    }

    public static void main(final String[] args) {
        try {
            Elements prev = null, next;
            while (true) {
                System.out.print("Checking Malvik... ");
                next = Jsoup.connect(URI).get().select(".clanek");
                if ((prev != null) && (!prev.equals(next))) {
                    System.out.println("and new offer(s) have been detected!");
                    Elements offers = new Elements();
                    for (final Element n : next) {
                        if (n.equals(prev.first())) break;
                        n.select("h2 a span").remove();
                        System.out.println("\n\n" + n.html() + "\n\n");
                        offers.add(n);
                    }
                    email(offers.html());
                } else {
                    System.out.println("and no change was detected.");
                }
                prev = next;
                Thread.sleep(HOURS);
            }
        } catch (final Exception e) {
            e.printStackTrace(System.err);
        }
    }
}
