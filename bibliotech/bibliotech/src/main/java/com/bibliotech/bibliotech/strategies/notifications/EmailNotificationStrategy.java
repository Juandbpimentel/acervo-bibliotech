package com.bibliotech.bibliotech.strategies.notifications;

import com.bibliotech.bibliotech.interfaces.NotificationStrategy;
import com.bibliotech.bibliotech.utils.EmailSend;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class EmailNotificationStrategy implements NotificationStrategy {
    private final EmailSend mailSender;

    @Autowired
    public EmailNotificationStrategy(EmailSend mailSender) {
        this.mailSender = mailSender;
    }
    @Override
    public boolean send(String recipient, String subject, String message) {
        mailSender.sendEmail(recipient, subject, message);
        return true;
    }
}
