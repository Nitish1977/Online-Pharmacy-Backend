package com.pharmacy.catalog.messaging;

import com.pharmacy.catalog.config.RabbitMQConfig;
import com.pharmacy.catalog.dto.OrderPlacedEvent;
import com.pharmacy.catalog.service.MedicineService;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class OrderEventListener {

    @Autowired
    private MedicineService medicineService;

    @RabbitListener(queues = RabbitMQConfig.QUEUE_STOCK)
    public void handleOrderPlaced(OrderPlacedEvent event) {
        if (event != null && event.getItems() != null) {
            medicineService.processOrderItems(event.getItems());
        }
    }
}
