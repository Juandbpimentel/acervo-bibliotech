package com.bibliotech.bibliotech.services;

import com.bibliotech.bibliotech.interfaces.NotificationStrategy;
import org.springframework.stereotype.Service;

@Service
public class NotificationService {
    private NotificationStrategy notificationStrategy;

    public NotificationService(NotificationStrategy notificationStrategy) {
        this.notificationStrategy = notificationStrategy;
    }

    public void setNotificationStrategy(NotificationStrategy notificationStrategy) {
        this.notificationStrategy = notificationStrategy;
    }

    public boolean sendNotification(String recipient, String subject, String message) {
        if (notificationStrategy == null) {
            throw new IllegalStateException("Notification strategy not set.");
        }
        return notificationStrategy.send(recipient, subject, message);
    }
}
