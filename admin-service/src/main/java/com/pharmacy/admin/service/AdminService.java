package com.pharmacy.admin.service;

import com.pharmacy.admin.dto.DashboardResponse;
import com.pharmacy.admin.dto.MedicineRequest;
import com.pharmacy.admin.entity.catalog.MedicineEntity;
import com.pharmacy.admin.entity.orders.OrderEntity;
import com.pharmacy.admin.enums.MedicineStatus;
import com.pharmacy.admin.enums.OrderStatus;
import com.pharmacy.admin.repository.catalog.MedicineEntityRepository;
import com.pharmacy.admin.repository.orders.OrderEntityRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Service
public class AdminService {

    // Reads from pharmacy_orders DB
    private final OrderEntityRepository orderRepository;

    // Reads from pharmacy_catalog DB
    private final MedicineEntityRepository medicineRepository;

    public AdminService(OrderEntityRepository orderRepository, MedicineEntityRepository medicineRepository) {
        this.orderRepository = orderRepository;
        this.medicineRepository = medicineRepository;
    }

    // ─── Dashboard ──────────────────────────────────────────────────────

    public DashboardResponse getDashboard() {

        // ── Order counts ────────────────────────────────────────────────
        long totalOrders     = orderRepository.count();
        long pendingOrders   = orderRepository.countByStatus(OrderStatus.PAYMENT_PENDING)
                             + orderRepository.countByStatus(OrderStatus.PAID)
                             + orderRepository.countByStatus(OrderStatus.PACKED);
        long inTransit       = orderRepository.countByStatus(OrderStatus.OUT_FOR_DELIVERY);
        long delivered       = orderRepository.countByStatus(OrderStatus.DELIVERED);
        long cancelled       = orderRepository.countByStatus(OrderStatus.CUSTOMER_CANCELLED)
                             + orderRepository.countByStatus(OrderStatus.ADMIN_CANCELLED);
        long rxPending       = orderRepository.countByStatus(OrderStatus.PRESCRIPTION_PENDING);

        // ── Revenue ─────────────────────────────────────────────────────
        Double revenue       = orderRepository.totalRevenue();
        double totalRevenue  = (revenue != null) ? revenue : 0.0;
        double avgOrderValue = delivered > 0 ? totalRevenue / delivered : 0.0;

        // ── Medicine counts ─────────────────────────────────────────────
        long totalMedicines  = medicineRepository.count();
        long activeMedicines = medicineRepository.countActiveMedicines();
        long lowStock        = medicineRepository.findByStockLessThan(10).size();
        long expiring        = medicineRepository.findExpiringBefore(
                                    LocalDate.now().plusDays(30)).size();

        return new DashboardResponse(
                totalOrders,
                pendingOrders,
                inTransit,
                delivered,
                cancelled,
                rxPending,
                totalRevenue,
                avgOrderValue,
                totalMedicines,
                activeMedicines,
                lowStock,
                expiring,
                0L   // prescription pending — from catalog DB, shown as 0 if not wired
        );
    }

    // ─── Medicine Management (writes to pharmacy_catalog) ───────────────

    public List<MedicineEntity> getAllMedicines() {
        return medicineRepository.findAll();
    }

    public MedicineEntity getMedicineById(Long id) {
        return medicineRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Medicine not found: " + id));
    }

    public MedicineEntity addMedicine(MedicineRequest req) {
        MedicineEntity m = new MedicineEntity();
        mapRequest(req, m);
        return medicineRepository.save(m);
    }

    public MedicineEntity updateMedicine(Long id, MedicineRequest req) {
        MedicineEntity m = getMedicineById(id);
        mapRequest(req, m);
        return medicineRepository.save(m);
    }

    public MedicineEntity updateStock(Long id, int quantity) {
        MedicineEntity m = getMedicineById(id);
        m.setStock(m.getStock() + quantity);
        return medicineRepository.save(m);
    }

    public void deleteMedicine(Long id) {
        MedicineEntity m = getMedicineById(id);
        m.setStatus(MedicineStatus.INACTIVE);
        medicineRepository.save(m);
    }

    public List<MedicineEntity> getLowStockMedicines() {
        return medicineRepository.findByStockLessThan(10);
    }

    public List<MedicineEntity> getExpiringMedicines(int days) {
        return medicineRepository.findExpiringBefore(LocalDate.now().plusDays(days));
    }

    private void mapRequest(MedicineRequest req, MedicineEntity m) {
        m.setName(req.getName());
        m.setBrand(req.getBrand());
        m.setCategory(req.getCategory());
        m.setDescription(req.getDescription());
        m.setPrice(req.getPrice());
        m.setStock(req.getStock());
        m.setRequiresPrescription(req.isRequiresPrescription());
        m.setExpiryDate(req.getExpiryDate());
        m.setDosage(req.getDosage());
        m.setImageUrl(req.getImageUrl());
    }

    // ─── Order Management (reads/writes to pharmacy_orders) ─────────────

    public List<OrderEntity> getAllOrders() {
        return orderRepository.findAllByOrderByCreatedAtDesc();
    }

    public OrderEntity getOrderById(Long id) {
        return orderRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Order not found: " + id));
    }

    public OrderEntity updateOrderStatus(Long id, String status) {
        OrderEntity order = getOrderById(id);
        try {
            order.setStatus(OrderStatus.valueOf(status.toUpperCase()));
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid status: " + status +
                ". Valid values: PACKED, OUT_FOR_DELIVERY, DELIVERED, ADMIN_CANCELLED, REFUND_INITIATED, REFUND_COMPLETED");
        }
        order.setUpdatedAt(LocalDateTime.now());
        return orderRepository.save(order);
    }

    public List<OrderEntity> getOrdersByStatus(String status) {
        try {
            return orderRepository.findByStatus(OrderStatus.valueOf(status.toUpperCase()));
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid status: " + status);
        }
    }

    // ─── Reports ────────────────────────────────────────────────────────

    public Map<String, Object> getSalesReport() {
        double revenue  = orderRepository.totalRevenue() != null ? orderRepository.totalRevenue() : 0;
        long delivered  = orderRepository.countByStatus(OrderStatus.DELIVERED);
        long total      = orderRepository.count();
        return Map.of(
            "totalRevenue",       revenue,
            "deliveredOrders",    delivered,
            "totalOrders",        total,
            "averageOrderValue",  delivered > 0 ? revenue / delivered : 0,
            "cancelledOrders",    orderRepository.countByStatus(OrderStatus.CUSTOMER_CANCELLED)
                                + orderRepository.countByStatus(OrderStatus.ADMIN_CANCELLED)
        );
    }

    public Map<String, Object> getInventoryReport() {
        return Map.of(
            "totalMedicines",    medicineRepository.count(),
            "activeMedicines",   medicineRepository.countActiveMedicines(),
            "lowStockMedicines", medicineRepository.findByStockLessThan(10),
            "expiringIn30Days",  medicineRepository.findExpiringBefore(LocalDate.now().plusDays(30))
        );
    }
}
