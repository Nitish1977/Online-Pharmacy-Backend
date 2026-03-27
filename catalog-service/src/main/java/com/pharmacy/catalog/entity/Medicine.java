package com.pharmacy.catalog.entity;

import com.pharmacy.catalog.enums.MedicineStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Entity
@Table(name = "medicines")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Medicine {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    private String brand;
    private String category;
    private String description;
    private double price;
    private int stock;

    @Column(name = "requires_prescription")
    private boolean requiresPrescription;

    @Column(name = "expiry_date")
    private LocalDate expiryDate;

    private String dosage;

    @Column(name = "image_url")
    private String imageUrl;

    @Enumerated(EnumType.STRING)
    private MedicineStatus status = MedicineStatus.ACTIVE;
}
