package com.pharmacy.admin.dto;

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

    public DashboardResponse() {}

    public DashboardResponse(long totalOrders, long pendingOrders, long inTransitOrders,
                             long deliveredOrders, long cancelledOrders, long prescriptionPendingOrders,
                             double totalRevenue, double averageOrderValue, long totalMedicines,
                             long activeMedicines, long lowStockCount, long expiringIn30DaysCount,
                             long pendingPrescriptions) {
        this.totalOrders = totalOrders;
        this.pendingOrders = pendingOrders;
        this.inTransitOrders = inTransitOrders;
        this.deliveredOrders = deliveredOrders;
        this.cancelledOrders = cancelledOrders;
        this.prescriptionPendingOrders = prescriptionPendingOrders;
        this.totalRevenue = totalRevenue;
        this.averageOrderValue = averageOrderValue;
        this.totalMedicines = totalMedicines;
        this.activeMedicines = activeMedicines;
        this.lowStockCount = lowStockCount;
        this.expiringIn30DaysCount = expiringIn30DaysCount;
        this.pendingPrescriptions = pendingPrescriptions;
    }

    public long getTotalOrders() { return totalOrders; }
    public void setTotalOrders(long totalOrders) { this.totalOrders = totalOrders; }

    public long getPendingOrders() { return pendingOrders; }
    public void setPendingOrders(long pendingOrders) { this.pendingOrders = pendingOrders; }

    public long getInTransitOrders() { return inTransitOrders; }
    public void setInTransitOrders(long inTransitOrders) { this.inTransitOrders = inTransitOrders; }

    public long getDeliveredOrders() { return deliveredOrders; }
    public void setDeliveredOrders(long deliveredOrders) { this.deliveredOrders = deliveredOrders; }

    public long getCancelledOrders() { return cancelledOrders; }
    public void setCancelledOrders(long cancelledOrders) { this.cancelledOrders = cancelledOrders; }

    public long getPrescriptionPendingOrders() { return prescriptionPendingOrders; }
    public void setPrescriptionPendingOrders(long prescriptionPendingOrders) { this.prescriptionPendingOrders = prescriptionPendingOrders; }

    public double getTotalRevenue() { return totalRevenue; }
    public void setTotalRevenue(double totalRevenue) { this.totalRevenue = totalRevenue; }

    public double getAverageOrderValue() { return averageOrderValue; }
    public void setAverageOrderValue(double averageOrderValue) { this.averageOrderValue = averageOrderValue; }

    public long getTotalMedicines() { return totalMedicines; }
    public void setTotalMedicines(long totalMedicines) { this.totalMedicines = totalMedicines; }

    public long getActiveMedicines() { return activeMedicines; }
    public void setActiveMedicines(long activeMedicines) { this.activeMedicines = activeMedicines; }

    public long getLowStockCount() { return lowStockCount; }
    public void setLowStockCount(long lowStockCount) { this.lowStockCount = lowStockCount; }

    public long getExpiringIn30DaysCount() { return expiringIn30DaysCount; }
    public void setExpiringIn30DaysCount(long expiringIn30DaysCount) { this.expiringIn30DaysCount = expiringIn30DaysCount; }

    public long getPendingPrescriptions() { return pendingPrescriptions; }
    public void setPendingPrescriptions(long pendingPrescriptions) { this.pendingPrescriptions = pendingPrescriptions; }
}
