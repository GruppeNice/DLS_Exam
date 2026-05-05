package com.engagementservice.sender;

import com.engagementservice.exception.NotificationException;
import com.engagementservice.model.Notification;
import com.engagementservice.types.NotificationType;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Component;
import org.thymeleaf.TemplateEngine;

@Component
public class EmailNotificationSender implements NotificationSender {

    private final JavaMailSender mailSender;

    private final TemplateEngine templateEngine;

    @Value("${notification.mail.from}")
    private String fromAddress;

    public EmailNotificationSender(JavaMailSender mailSender, TemplateEngine templateEngine) {
        this.mailSender = mailSender;
        this.templateEngine = templateEngine;
    }

    @Override
    public NotificationType getType() {
        return NotificationType.EMAIL;
    }

    @Override
    public void send(Notification notification) throws NotificationException {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(fromAddress);
            helper.setTo(notification.getRecipient());
            helper.setSubject(notification.getSubject());
            helper.setText(notification.getContent(), true); // HTML content

            mailSender.send(message);

        } catch (MessagingException e) {
            throw new NotificationException("Failed to send email", e);
        }
    }
}
