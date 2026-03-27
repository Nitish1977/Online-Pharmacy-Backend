package com.pharmacy.order.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

@Data
public class CartRequest {

    @NotNull(message = "Customer ID is required")
    private Long customerId;

    @NotNull(message = "Medicine ID is required")
    private Long medicineId;

    private String medicineName;

    @Positive(message = "Quantity must be positive")
    private int quantity;

    private double unitPrice;

    private boolean requiresPrescription;
}
