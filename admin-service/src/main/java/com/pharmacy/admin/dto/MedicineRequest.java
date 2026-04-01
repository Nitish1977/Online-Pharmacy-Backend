package com.pharmacy.admin.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;

import java.time.LocalDate;

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

    public MedicineRequest() {}

    public MedicineRequest(String name, String brand, String category, String description, double price, int stock, boolean requiresPrescription, LocalDate expiryDate, String dosage, String imageUrl) {
        this.name = name;
        this.brand = brand;
        this.category = category;
        this.description = description;
        this.price = price;
        this.stock = stock;
        this.requiresPrescription = requiresPrescription;
        this.expiryDate = expiryDate;
        this.dosage = dosage;
        this.imageUrl = imageUrl;
    }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getBrand() { return brand; }
    public void setBrand(String brand) { this.brand = brand; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public double getPrice() { return price; }
    public void setPrice(double price) { this.price = price; }

    public int getStock() { return stock; }
    public void setStock(int stock) { this.stock = stock; }

    public boolean isRequiresPrescription() { return requiresPrescription; }
    public void setRequiresPrescription(boolean requiresPrescription) { this.requiresPrescription = requiresPrescription; }

    public LocalDate getExpiryDate() { return expiryDate; }
    public void setExpiryDate(LocalDate expiryDate) { this.expiryDate = expiryDate; }

    public String getDosage() { return dosage; }
    public void setDosage(String dosage) { this.dosage = dosage; }

    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }
}
