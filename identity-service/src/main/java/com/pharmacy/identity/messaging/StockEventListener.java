package com.pharmacy.identity.messaging;

import com.pharmacy.identity.config.RabbitMQConfig;
import com.pharmacy.identity.dto.LowStockEvent;
import com.pharmacy.identity.entity.User;
import com.pharmacy.identity.enums.Role;
import com.pharmacy.identity.repository.UserRepository;
import com.pharmacy.identity.service.EmailService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class StockEventListener {

    private static final Logger log = LoggerFactory.getLogger(StockEventListener.class);

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private EmailService emailService;

    @Autowired
    private ObjectMapper objectMapper;

    @RabbitListener(queues = RabbitMQConfig.QUEUE_EMAIL)
    public void handleLowStockAlert(Message message) {
        try {
            LowStockEvent event = objectMapper.readValue(message.getBody(), LowStockEvent.class);
            log.info("Received low stock alert for {}: {} items remaining", event.getMedicineName(), event.getRemainingStock());
            
            List<User> admins = userRepository.findByRole(Role.ADMIN);
            
            for (User admin : admins) {
                emailService.sendLowStockAlert(admin.getEmail(), event.getMedicineName(), event.getRemainingStock());
            }
        } catch (Exception e) {
            log.error("Failed to parse low stock alert message", e);
        }
    }
}
