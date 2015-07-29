import com.google.appengine.api.datastore.*;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import java.util.Properties;
import java.util.logging.Logger;

public class Malvik {

    private static final Logger log = Logger.getLogger(Malvik.class.getName());

    private static final String BASE_URI = "https://www.malvik.cz";
    private static final String RESOURCE_URI = "/Tema/Aktuality-z-Malvika/";
    private static final String URI = BASE_URI + RESOURCE_URI;

//    private static final long HOURS = 1000 * 60 * 60 * 4;
//    private static final String SMTP_HOST_NAME = "smtp.gmail.com";

    private static final String EMAIL_FROM = "malvik-angel@broulik-malvik.appspotmail.com";
    private static final String[] EMAIL_TO = new String[]{"thehurda@gmail.com", "me@rrc.io"};

//    private static final String SMTP_AUTH_PWD = System.getenv("GMAIL_PASSWORD");

    private static final Properties properties = new Properties();

//    static {
//        properties.put("mail.transport.protocol", "smtp");
//        properties.put("mail.smtp.host", SMTP_HOST_NAME);
//        properties.put("mail.smtp.port", 587);
//        properties.put("mail.smtp.auth", "true");
//        properties.put("mail.smtp.starttls.enable", "true");
//    }

    private static final Session session = Session.getInstance(properties, null);
    public static final String ELEMENTS_KIND = "Elements";
    public static final int ELEMENTS_ID = 666;
    public static final String PROPERTY_NAME = "html";

    private static void email(final String offers) {
        try {

            final MimeMessage message = new MimeMessage(session);
            final Multipart multipart = new MimeMultipart();
            final BodyPart body = new MimeBodyPart();
            body.setContent("<html>" +
                            "<meta charset=\"UTF-8\"></meta>" +
                            "<body>" + offers.replace("href=\"/", "href=\"" + BASE_URI + "/") + "</body>" +
                            "</html>",
                    "text/html; charset=utf-8");
            multipart.addBodyPart(body);
            message.setFrom(new InternetAddress(EMAIL_FROM));
            message.addRecipients(Message.RecipientType.TO, new Address[]{
                    new InternetAddress(EMAIL_TO[0]),
                    new InternetAddress(EMAIL_TO[1])
            });
            message.setSubject("Malvik sale!");
            message.setContent(multipart);
            Transport.send(message);
            log.info("An e-mail containing the new offers has been sent.");
        } catch (final MessagingException e) {
            e.printStackTrace(System.err);
        }
    }

    public static void main(final String[] args) {
        try {
            Elements prev = getArticles();


            Elements next = Jsoup.connect(URI).get().select(".clanek");
            log.info("Checking Malvik... old artilces: " + prev.size() + "new articles: " + next.size());

            if ((prev != null) && (!prev.outerHtml().equals(next.outerHtml()))) {
                log.info("and new offer(s) have been detected!");
                Elements offers = new Elements();
                for (final Element n : next) {
                    log.info("\nNEW:\n" + n.html() + "\n\n");
                    log.info("\nFIRST:\n" + prev.first().html() + "\n\n");

                    if (n.outerHtml().equals(prev.first().outerHtml())) {
                        log.info("BREAK");
                        break;
                    }

//                    n.select("h2 a span").remove();
                    log.info("\nADDING TO EMAIL\n" + n.outerHtml() + "\n\n");
                    offers.add(n);
                }
                email(offers.html());
            } else {
                log.info("and no change was detected.");
            }

            saveArticles(next);
        } catch (final Exception e) {
            e.printStackTrace(System.err);
            throw new RuntimeException(e);
        }
    }

    private static void saveArticles(Elements elements) {
        DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();

        Entity elementsEntity = new Entity(ELEMENTS_KIND, ELEMENTS_ID);
        elementsEntity.setProperty(PROPERTY_NAME, new Text(elements.outerHtml()));

        datastore.put(elementsEntity);
    }

    private static Elements getArticles() {
        DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();

        try {
            Entity entity = datastore.get(KeyFactory.createKey(ELEMENTS_KIND, ELEMENTS_ID));
            String html = ((Text) entity.getProperty(PROPERTY_NAME)).getValue();
//            log.info("Extracted: " + html);
            return Jsoup.parse(html).select(".clanek");

        } catch (EntityNotFoundException e) {
            return null;
        }


    }

}
