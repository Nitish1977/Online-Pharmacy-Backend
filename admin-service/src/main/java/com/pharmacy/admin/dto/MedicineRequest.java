package com.pharmacy.admin.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.Data;

import java.time.LocalDate;

@Data
public class MedicineRequest {

    @NotBlank(message = "Name is required")
    private String name;

    private String brand;

    @NotBlank(message = "Category is required")
    private String category;

    private String description;

    @Positive(message = "Price must be positive")
    private double price;

    @PositiveOrZero
    private int stock;

    private boolean requiresPrescription;
    private LocalDate expiryDate;
    private String dosage;
    private String imageUrl;
}
