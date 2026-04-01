package com.pharmacy.identity.messaging;

import com.pharmacy.identity.config.RabbitMQConfig;
import com.pharmacy.identity.dto.LowStockEvent;
import com.pharmacy.identity.entity.User;
import com.pharmacy.identity.enums.Role;
import com.pharmacy.identity.repository.UserRepository;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class StockEventListener {

    private static final Logger log = LoggerFactory.getLogger(StockEventListener.class);

    private final UserRepository userRepository;
    private final RabbitTemplate rabbitTemplate;
    private final ObjectMapper objectMapper;

    public StockEventListener(UserRepository userRepository, RabbitTemplate rabbitTemplate, ObjectMapper objectMapper) {
        this.userRepository = userRepository;
        this.rabbitTemplate = rabbitTemplate;
        this.objectMapper = objectMapper;
    }

    @RabbitListener(queues = RabbitMQConfig.QUEUE_STOCK_LOW)
    public void handleLowStockAlert(Message message) {
        try {
            LowStockEvent event = objectMapper.readValue(message.getBody(), LowStockEvent.class);
            log.info("Received low stock alert for {}: {} items remaining", event.getMedicineName(), event.getRemainingStock());
            
            List<User> admins = userRepository.findByRole(Role.ADMIN);
            
            for (User admin : admins) {
                com.pharmacy.identity.dto.NotificationEvent notification = new com.pharmacy.identity.dto.NotificationEvent();
                notification.setType(com.pharmacy.identity.dto.NotificationType.LOW_STOCK);
                notification.setRecipientEmail(admin.getEmail());
                notification.setPayload(java.util.Map.of("medicineName", event.getMedicineName(), "remainingStock", event.getRemainingStock()));
                
                rabbitTemplate.convertAndSend(RabbitMQConfig.EXCHANGE, RabbitMQConfig.ROUTING_KEY_NOTIFICATION, notification);
            }
        } catch (Exception e) {
            log.error("Failed to parse low stock alert message", e);
        }
    }
}
