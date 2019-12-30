package az.gov.adra.util;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Component;

@Component
public class EmailSenderUtil {

    @Autowired
    private JavaMailSender emailSender;

    public void sendEmailMessage(String to, String subject, String body) {
        try {
//            MimeMessage message = emailSender.createMimeMessage();
//            message.setHeader("Content-Type", "text/plain; charset=UTF-8");
//            message.setRecipient(Message.RecipientType.TO, new InternetAddress(to));
//            message.setSubject(subject, "utf-8");
//            message.setContent(body, "utf-8");
//            emailSender.send(message);

            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(to);
            message.setSubject(subject);
            message.setText(body);
            emailSender.send(message);

        } catch (Exception e) {
            e.printStackTrace();
        }

    }

}
