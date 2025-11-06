package com.bibliotech.bibliotech.interfaces;

public interface NotificationStrategy {
    boolean send (String recipient, String subject, String message);
}
