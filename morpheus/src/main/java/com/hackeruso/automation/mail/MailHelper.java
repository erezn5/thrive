package com.hackeruso.automation.mail;


import com.hackeruso.automation.conf.EnvConf;
import com.hackeruso.automation.utils.Waiter;
import org.apache.log4j.Logger;
import org.apache.log4j.spi.RootLogger;
import org.awaitility.Duration;
import org.awaitility.core.Condition;
import javax.mail.*;
import java.io.IOException;
import java.util.Date;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MailHelper {
    private final static Logger Log = RootLogger.getLogger(MailHelper.class);
    private static final Properties IMAP_PROPERTIES = new Properties();
    private static final Properties POP3_PROPERTIES = new Properties();

    static {
        IMAP_PROPERTIES.put("mail.store.protocol", "imaps");
        IMAP_PROPERTIES.put("mail.imaps.host", "imap.gmail.com");
        IMAP_PROPERTIES.put("mail.host", "imap.gmail.com");
        IMAP_PROPERTIES.put("mail.imaps.port", "993");
        IMAP_PROPERTIES.put("mail.imaps.starttls.enable", "true");

        POP3_PROPERTIES.put("mail.store.protocol", "pop3s");
        POP3_PROPERTIES.put("mail.pop3s.host", "pop.gmail.com");
        POP3_PROPERTIES.put("mail.host", "pop.gmail.com");
        POP3_PROPERTIES.put("mail.pop3s.port", "995");
        POP3_PROPERTIES.put("mail.pop3s.starttls.enable", "true");
    }

    public static String getMessagesFromGmail(String email, String password, String pFrom, String pSubject, Date afterDate) {
        return getMessagesFromGmail(StoreType.IMAPS, email, password, pFrom, pSubject, afterDate);
    }

    private static String getMessagesFromGmail(StoreType type, String email, String password, String from, String subject, Date afterDate) {
        Condition<String>condition= ()-> {
            Folder folder;

            try {
                folder = connectToMailBox(type, email, password);

                if (!folder.isOpen()) {
                    folder.open(Folder.READ_ONLY);
                }
                return getMessage(from, subject, afterDate, folder);
            } catch (Exception e) {
                Log.error(e.getMessage());
                return null;
            }
        };
        String content = Waiter.waitCondition(Duration.FIVE_MINUTES, condition, Duration.ONE_MINUTE);
        if(content == null){
            throw new IllegalStateException("failed to get message");
        }
        return content;

    }

    private static String getMessage(String from, String subject, Date afterDate, Folder folder) throws MessagingException, IOException {
        Message[] messages = folder.getMessages();
        for (Message message : messages) {
            if (message.getSentDate().after(afterDate) && message.getSubject().contains(subject) && message.getFrom()[0].toString().contains(from)) {
                return message.getContent().toString();
            }
        }
        return null;
    }

    private static void close(Folder folder , Store store) throws MessagingException {
        if (folder != null &&
                folder.isOpen()) {
            folder.close(true);
        }
        if (store != null) {
            store.close();
        }
    }

    private static Folder connectToMailBox(StoreType type, String email, String password) throws MessagingException {
        Store store;
        Folder folder;
        Session session = Session.getDefaultInstance(type.properties, null);
        store = session.getStore(type.properties.getProperty("mail.store.protocol"));
        store.connect(type.properties.getProperty("mail.host"), email, password);
        folder = store.getFolder("INBOX");
        return folder;
    }

    public enum StoreType {
        IMAPS(IMAP_PROPERTIES),
        POP3S(POP3_PROPERTIES);

        private final Properties properties;

        StoreType(Properties properties) {
            this.properties = properties;
        }
    }

    public static String verifyEmail(String userEmail, String userEmailPassword, Date testStartTime, String subject){
        String verifyEmailURL = "";
        String ENVIRONMENT_BASE_URL = EnvConf.getProperty("base.url");
        String regex = "href=\"([^\"]*)";
        String from = "cyber@cywar.hackeru.com";
        String msg = MailHelper.getMessagesFromGmail( userEmail, userEmailPassword, from, subject, testStartTime);
        Pattern linkPattern = Pattern.compile(regex, Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
        Matcher pageMatcher = linkPattern.matcher(msg);
        while (pageMatcher.find()) {
            if (pageMatcher.group(1).contains("reset-password")) {
                verifyEmailURL = pageMatcher.group(1);
                verifyEmailURL = ENVIRONMENT_BASE_URL.concat("/").concat(verifyEmailURL.split("/")[3]);
                Log.info("Verify URL: " + verifyEmailURL);
            }
        }
        Log.info("Verifying email address is successful!");
        return verifyEmailURL;
    }
}
