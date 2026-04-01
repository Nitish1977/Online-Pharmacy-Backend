package com.pharmacy.notification.consumer;

import com.pharmacy.notification.config.RabbitMQConfig;
import com.pharmacy.notification.dto.NotificationEvent;
import com.pharmacy.notification.service.EmailService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class EmailConsumer {

    private final EmailService emailService;

    @RabbitListener(queues = RabbitMQConfig.QUEUE_NOTIFICATION)
    public void consumeMessage(NotificationEvent event) {
        log.info("Received notification event: {}", event.getType());

        try {
            switch (event.getType()) {
                case WELCOME:
                    Map<String, Object> payloadWelcome = event.getPayload();
                    String name = payloadWelcome != null && payloadWelcome.containsKey("name") ? payloadWelcome.get("name").toString() : "User";
                    emailService.sendWelcomeEmail(event.getRecipientEmail(), name);
                    break;
                case LOW_STOCK:
                    Map<String, Object> payloadStock = event.getPayload();
                    String medicineName = payloadStock != null && payloadStock.containsKey("medicineName") ? payloadStock.get("medicineName").toString() : "Unknown";
                    int remainingStock = payloadStock != null && payloadStock.containsKey("remainingStock") ? Integer.parseInt(payloadStock.get("remainingStock").toString()) : 0;
                    emailService.sendLowStockAlert(event.getRecipientEmail(), medicineName, remainingStock);
                    break;
                default:
                    log.warn("Unknown notification type: {}", event.getType());
            }
        } catch (Exception e) {
            log.error("Error processing notification event: {}", e.getMessage(), e);
        }
    }
}
