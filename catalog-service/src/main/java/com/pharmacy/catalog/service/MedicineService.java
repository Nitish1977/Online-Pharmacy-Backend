package com.pharmacy.catalog.service;

import com.pharmacy.catalog.dto.MedicineRequest;
import com.pharmacy.catalog.entity.Medicine;
import com.pharmacy.catalog.enums.MedicineStatus;
import com.pharmacy.catalog.repository.MedicineRepository;
import com.pharmacy.catalog.config.RabbitMQConfig;
import com.pharmacy.catalog.dto.LowStockEvent;
import com.pharmacy.catalog.dto.OrderPlacedEvent;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class MedicineService {

    @Autowired
    private MedicineRepository medicineRepository;

    @Autowired
    private RabbitTemplate rabbitTemplate;

    public List<Medicine> getAllMedicines() {
        return medicineRepository.findByStatus(MedicineStatus.ACTIVE);
    }

    public Medicine getMedicineById(Long id) {
        return medicineRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Medicine not found with id: " + id));
    }

    public List<Medicine> searchMedicines(String keyword) {
        if (keyword == null || keyword.trim().isEmpty())
            return getAllMedicines();
        return medicineRepository.searchMedicines(keyword.trim());
    }

    public List<Medicine> getMedicinesByCategory(String category) {
        return medicineRepository.findByCategory(category);
    }

    public Medicine addMedicine(MedicineRequest request) {
        Medicine medicine = new Medicine();
        mapToEntity(request, medicine);
        return medicineRepository.save(medicine);
    }

    public Medicine updateMedicine(Long id, MedicineRequest request) {
        Medicine medicine = getMedicineById(id);
        mapToEntity(request, medicine);
        return medicineRepository.save(medicine);
    }

    public void deleteMedicine(Long id) {
        Medicine medicine = getMedicineById(id);
        medicine.setStatus(MedicineStatus.INACTIVE);
        medicineRepository.save(medicine);
    }

    private void mapToEntity(MedicineRequest request, Medicine medicine) {
        medicine.setName(request.getName());
        medicine.setBrand(request.getBrand());
        medicine.setCategory(request.getCategory());
        medicine.setDescription(request.getDescription());
        medicine.setPrice(request.getPrice());
        medicine.setStock(request.getStock());
        medicine.setRequiresPrescription(request.isRequiresPrescription());
        medicine.setExpiryDate(request.getExpiryDate());
        medicine.setDosage(request.getDosage());
        medicine.setImageUrl(request.getImageUrl());
    }

    @Transactional
    public void processOrderItems(List<OrderPlacedEvent.OrderItemDto> items) {
        for (OrderPlacedEvent.OrderItemDto item : items) {
            Medicine medicine = medicineRepository.findById(item.getMedicineId()).orElse(null);
            if (medicine != null) {
                int remaining = medicine.getStock() - item.getQuantity();
                medicine.setStock(remaining >= 0 ? remaining : 0);
                medicineRepository.save(medicine);

                if (medicine.getStock() <= 5) {
                    LowStockEvent event = new LowStockEvent(medicine.getId(), medicine.getName(), medicine.getStock());
                    rabbitTemplate.convertAndSend(RabbitMQConfig.EXCHANGE, RabbitMQConfig.ROUTING_KEY_STOCK_LOW, event);
                }
            }
        }
    }
}
