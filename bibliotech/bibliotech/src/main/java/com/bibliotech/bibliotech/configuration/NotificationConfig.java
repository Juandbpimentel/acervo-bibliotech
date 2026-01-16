package com.bibliotech.bibliotech.configuration;

import com.bibliotech.bibliotech.interfaces.NotificationStrategy;
import com.bibliotech.bibliotech.strategies.notifications.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.lang.Nullable;

@Configuration
public class NotificationConfig {

    @Value("${notification.strategy:email}")
    private String notificationStrategy;

    @Value("${notification.multi.require-all-success:false}")
    private boolean requireAllSuccess;

    // Use constructor injection with nullable params so application can start even if optional
    // notification implementations (e.g., email) are not available in the environment.
    private final EmailNotificationStrategy emailStrategy;
    private final SMSNotificationStrategy smsStrategy;
    private final WhatsAppNotificationStrategy whatsAppStrategy;

    @Autowired
    public NotificationConfig(
            @Nullable EmailNotificationStrategy emailStrategy,
            @Nullable SMSNotificationStrategy smsStrategy,
            @Nullable WhatsAppNotificationStrategy whatsAppStrategy) {
        this.emailStrategy = emailStrategy;
        this.smsStrategy = smsStrategy;
        this.whatsAppStrategy = whatsAppStrategy;
    }

    @Bean
    @Primary
    public NotificationStrategy notificationStrategy() {
        String chosen = notificationStrategy == null ? "email" : notificationStrategy.toLowerCase();

        switch (chosen) {
            case "sms":
                System.out.println("✓ Estratégia de notificação: SMS");
                if (smsStrategy == null) {
                    System.out.println("⚠️ SMS strategy não encontrada. Usando NoOp.");
                    return noOpStrategy("SMS");
                }
                return smsStrategy;
            case "whatsapp":
                System.out.println("✓ Estratégia de notificação: WhatsApp");
                if (whatsAppStrategy == null) {
                    System.out.println("⚠️ WhatsApp strategy não encontrada. Usando NoOp.");
                    return noOpStrategy("WhatsApp");
                }
                return whatsAppStrategy;
            case "multi":
                System.out.println("✓ Estratégia de notificação: Multi-canal");
                return createMultiChannelStrategy();
            default:
                System.out.println("✓ Estratégia de notificação: Email (padrão)");
                if (emailStrategy == null) {
                    System.out.println("⚠️ Email strategy não encontrada. Usando NoOp.");
                    return noOpStrategy("Email");
                }
                return emailStrategy;
        }
    }

    private NotificationStrategy createMultiChannelStrategy() {
        MultiChannelNotificationStrategy multiChannel = new MultiChannelNotificationStrategy();

        if (emailStrategy != null) {
            multiChannel.addStrategy(emailStrategy);
            System.out.println("  - Email: ativado");
        } else {
            System.out.println("  - Email: ausente (NoOp)");
        }

        if (smsStrategy != null) {
            multiChannel.addStrategy(smsStrategy);
            System.out.println("  - SMS: ativado");
        } else {
            System.out.println("  - SMS: ausente (NoOp)");
        }

        if (whatsAppStrategy != null) {
            multiChannel.addStrategy(whatsAppStrategy);
            System.out.println("  - WhatsApp: ativado");
        } else {
            System.out.println("  - WhatsApp: ausente (NoOp)");
        }

        multiChannel.setRequireAllSuccess(requireAllSuccess);

        System.out.println("  - Requer sucesso em todos: " + requireAllSuccess);

        return multiChannel;
    }

    private NotificationStrategy noOpStrategy(String name) {
        return (recipient, subject, message) -> {
            System.out.printf("[NoOp-%s] Notificação simulada para %s\n", name, recipient);
            return false;
        };
    }

    @Bean(name = "emailNotificationStrategy")
    public NotificationStrategy emailNotificationStrategy() {
        return emailStrategy;
    }

    @Bean(name = "smsNotificationStrategy")
    public NotificationStrategy smsNotificationStrategy() {
        return smsStrategy;
    }

    @Bean(name = "whatsappNotificationStrategy")
    public NotificationStrategy whatsappNotificationStrategy() {
        return whatsAppStrategy;
    }
}