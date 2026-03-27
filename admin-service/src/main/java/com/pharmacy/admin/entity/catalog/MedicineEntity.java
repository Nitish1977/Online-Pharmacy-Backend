package com.pharmacy.admin.entity.catalog;

import com.pharmacy.admin.enums.MedicineStatus;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Entity
@Table(name = "medicines")
@Getter
@Setter
@NoArgsConstructor
public class MedicineEntity {

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
