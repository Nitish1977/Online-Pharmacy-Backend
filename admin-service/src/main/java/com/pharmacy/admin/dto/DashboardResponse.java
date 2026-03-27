package com.pharmacy.admin.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class DashboardResponse {

    // ── Order KPIs ──────────────────────────────────────────────────────
    private long totalOrders;
    private long pendingOrders;         // PAYMENT_PENDING + PAID + PACKED
    private long inTransitOrders;       // OUT_FOR_DELIVERY
    private long deliveredOrders;
    private long cancelledOrders;
    private long prescriptionPendingOrders;

    // ── Revenue ─────────────────────────────────────────────────────────
    private double totalRevenue;
    private double averageOrderValue;

    // ── Inventory KPIs ──────────────────────────────────────────────────
    private long totalMedicines;
    private long activeMedicines;
    private long lowStockCount;         // stock < 10
    private long expiringIn30DaysCount;

    // ── Prescription KPIs ───────────────────────────────────────────────
    private long pendingPrescriptions;
}
