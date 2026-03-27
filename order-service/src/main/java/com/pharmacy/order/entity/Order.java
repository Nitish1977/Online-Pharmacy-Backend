package com.pharmacy.order.entity;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.pharmacy.order.enums.OrderStatus;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "orders")
@Getter
@Setter
@NoArgsConstructor
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "customer_id", nullable = false)
    private Long customerId;

    @Enumerated(EnumType.STRING)
    private OrderStatus status = OrderStatus.DRAFT_CART;

    @Column(name = "total_amount")
    private double totalAmount;

    @Column(name = "delivery_address")
    private String deliveryAddress;

    private String pincode;

    @Column(name = "delivery_slot")
    private String deliverySlot;

    @Column(name = "prescription_id")
    private Long prescriptionId;

    @Column(name = "payment_id")
    private String paymentId;

    @Column(name = "created_at")
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "updated_at")
    private LocalDateTime updatedAt = LocalDateTime.now();

    // @JsonManagedReference tells Jackson: serialize this side (the parent)
    // and stop here — do NOT follow back-references from children
    @JsonManagedReference
    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    private List<OrderItem> items = new ArrayList<>();
}
