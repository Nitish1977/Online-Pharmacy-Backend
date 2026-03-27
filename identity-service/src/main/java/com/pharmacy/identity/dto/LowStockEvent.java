package com.pharmacy.identity.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LowStockEvent {
    private Long medicineId;
    private String medicineName;
    private int remainingStock;
}
