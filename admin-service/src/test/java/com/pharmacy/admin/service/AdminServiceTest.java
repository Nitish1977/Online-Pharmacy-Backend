package com.pharmacy.admin.service;

import com.pharmacy.admin.dto.DashboardResponse;
import com.pharmacy.admin.dto.MedicineRequest;
import com.pharmacy.admin.entity.catalog.MedicineEntity;
import com.pharmacy.admin.entity.orders.OrderEntity;
import com.pharmacy.admin.enums.MedicineStatus;
import com.pharmacy.admin.enums.OrderStatus;
import com.pharmacy.admin.repository.catalog.MedicineEntityRepository;
import com.pharmacy.admin.repository.orders.OrderEntityRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("AdminService Unit Tests")
class AdminServiceTest {

    @Mock private OrderEntityRepository orderRepository;
    @Mock private MedicineEntityRepository medicineRepository;

    @InjectMocks
    private AdminService adminService;

    private OrderEntity mockOrder;
    private MedicineEntity mockMedicine;

    @BeforeEach
    void setUp() {
        mockOrder = new OrderEntity();
        mockOrder.setId(1L);
        mockOrder.setCustomerId(1L);
        mockOrder.setStatus(OrderStatus.PAID);
        mockOrder.setTotalAmount(51.00);
        mockOrder.setCreatedAt(LocalDateTime.now());

        mockMedicine = new MedicineEntity();
        mockMedicine.setId(1L);
        mockMedicine.setName("Paracetamol 500mg");
        mockMedicine.setStock(100);
        mockMedicine.setStatus(MedicineStatus.ACTIVE);
        mockMedicine.setExpiryDate(LocalDate.now().plusMonths(6));
    }

    // ── Dashboard Tests ──────────────────────────────────────────────────────

    @Test
    @DisplayName("TC-01: Dashboard returns correct order and medicine KPIs")
    void getDashboard_ReturnsCorrectKPIs() {
        when(orderRepository.count()).thenReturn(5L);
        when(orderRepository.countByStatus(OrderStatus.PAYMENT_PENDING)).thenReturn(1L);
        when(orderRepository.countByStatus(OrderStatus.PAID)).thenReturn(1L);
        when(orderRepository.countByStatus(OrderStatus.PACKED)).thenReturn(0L);
        when(orderRepository.countByStatus(OrderStatus.OUT_FOR_DELIVERY)).thenReturn(1L);
        when(orderRepository.countByStatus(OrderStatus.DELIVERED)).thenReturn(2L);
        when(orderRepository.countByStatus(OrderStatus.CUSTOMER_CANCELLED)).thenReturn(0L);
        when(orderRepository.countByStatus(OrderStatus.ADMIN_CANCELLED)).thenReturn(0L);
        when(orderRepository.countByStatus(OrderStatus.PRESCRIPTION_PENDING)).thenReturn(0L);
        when(orderRepository.totalRevenue()).thenReturn(102.00);

        when(medicineRepository.count()).thenReturn(10L);
        when(medicineRepository.countActiveMedicines()).thenReturn(8L);
        when(medicineRepository.findByStockLessThan(10)).thenReturn(List.of());
        when(medicineRepository.findExpiringBefore(any(LocalDate.class))).thenReturn(List.of(mockMedicine));

        DashboardResponse response = adminService.getDashboard();

        assertThat(response.getTotalOrders()).isEqualTo(5L);
        assertThat(response.getPendingOrders()).isEqualTo(2L);   // PAYMENT_PENDING + PAID + PACKED
        assertThat(response.getInTransitOrders()).isEqualTo(1L);
        assertThat(response.getDeliveredOrders()).isEqualTo(2L);
        assertThat(response.getTotalRevenue()).isEqualTo(102.00);
        assertThat(response.getAverageOrderValue()).isEqualTo(51.00); // 102 / 2
        assertThat(response.getTotalMedicines()).isEqualTo(10L);
        assertThat(response.getActiveMedicines()).isEqualTo(8L);
        assertThat(response.getLowStockCount()).isZero();
        assertThat(response.getExpiringIn30DaysCount()).isEqualTo(1L);
    }

    @Test
    @DisplayName("TC-02: Dashboard with no orders shows zero revenue")
    void getDashboard_NoOrders_ShowsZeroRevenue() {
        when(orderRepository.count()).thenReturn(0L);
        when(orderRepository.countByStatus(any())).thenReturn(0L);
        when(orderRepository.totalRevenue()).thenReturn(null);
        when(medicineRepository.count()).thenReturn(0L);
        when(medicineRepository.countActiveMedicines()).thenReturn(0L);
        when(medicineRepository.findByStockLessThan(10)).thenReturn(List.of());
        when(medicineRepository.findExpiringBefore(any())).thenReturn(List.of());

        DashboardResponse response = adminService.getDashboard();

        assertThat(response.getTotalRevenue()).isZero();
        assertThat(response.getAverageOrderValue()).isZero();
        assertThat(response.getTotalOrders()).isZero();
    }

    // ── Medicine Tests ───────────────────────────────────────────────────────

    @Test
    @DisplayName("TC-03: Get all medicines returns full list")
    void getAllMedicines_ReturnsFullList() {
        when(medicineRepository.findAll()).thenReturn(List.of(mockMedicine));

        List<MedicineEntity> result = adminService.getAllMedicines();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getName()).isEqualTo("Paracetamol 500mg");
    }

    @Test
    @DisplayName("TC-04: Add medicine saves new medicine to catalog DB")
    void addMedicine_ValidRequest_SavesToCatalogDb() {
        MedicineRequest request = new MedicineRequest();
        request.setName("Vitamin C");
        request.setCategory("Vitamins");
        request.setPrice(120.0);
        request.setStock(200);

        when(medicineRepository.save(any(MedicineEntity.class))).thenReturn(mockMedicine);

        MedicineEntity result = adminService.addMedicine(request);

        assertThat(result).isNotNull();
        verify(medicineRepository).save(any(MedicineEntity.class));
    }

    @Test
    @DisplayName("TC-05: Update stock adds quantity to current stock")
    void updateStock_PositiveQuantity_IncreasesStock() {
        mockMedicine.setStock(50);

        when(medicineRepository.findById(1L)).thenReturn(Optional.of(mockMedicine));
        when(medicineRepository.save(any(MedicineEntity.class))).thenReturn(mockMedicine);

        adminService.updateStock(1L, 30);

        verify(medicineRepository).save(argThat(m -> m.getStock() == 80));
    }

    @Test
    @DisplayName("TC-06: Update stock with negative quantity reduces stock")
    void updateStock_NegativeQuantity_DecreasesStock() {
        mockMedicine.setStock(50);

        when(medicineRepository.findById(1L)).thenReturn(Optional.of(mockMedicine));
        when(medicineRepository.save(any(MedicineEntity.class))).thenReturn(mockMedicine);

        adminService.updateStock(1L, -10);

        verify(medicineRepository).save(argThat(m -> m.getStock() == 40));
    }

    @Test
    @DisplayName("TC-07: Delete medicine sets status to INACTIVE")
    void deleteMedicine_ValidId_SetsInactive() {
        when(medicineRepository.findById(1L)).thenReturn(Optional.of(mockMedicine));
        when(medicineRepository.save(any(MedicineEntity.class))).thenReturn(mockMedicine);

        adminService.deleteMedicine(1L);

        verify(medicineRepository).save(argThat(m -> m.getStatus() == MedicineStatus.INACTIVE));
    }

    @Test
    @DisplayName("TC-08: Get low stock medicines returns medicines below threshold")
    void getLowStockMedicines_ReturnsBelowThreshold() {
        MedicineEntity lowStock = new MedicineEntity();
        lowStock.setId(2L);
        lowStock.setName("Aspirin 100mg");
        lowStock.setStock(5);

        when(medicineRepository.findByStockLessThan(10)).thenReturn(List.of(lowStock));

        List<MedicineEntity> result = adminService.getLowStockMedicines();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getStock()).isLessThan(10);
    }

    // ── Order Tests ──────────────────────────────────────────────────────────

    @Test
    @DisplayName("TC-09: Update order status to PACKED succeeds")
    void updateOrderStatus_ValidStatus_UpdatesOrder() {
        when(orderRepository.findById(1L)).thenReturn(Optional.of(mockOrder));
        when(orderRepository.save(any(OrderEntity.class))).thenReturn(mockOrder);

        adminService.updateOrderStatus(1L, "PACKED");

        verify(orderRepository).save(argThat(o -> o.getStatus() == OrderStatus.PACKED));
    }

    @Test
    @DisplayName("TC-10: Update order with invalid status throws exception")
    void updateOrderStatus_InvalidStatus_ThrowsException() {
        when(orderRepository.findById(1L)).thenReturn(Optional.of(mockOrder));

        assertThatThrownBy(() -> adminService.updateOrderStatus(1L, "FLYING"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Invalid status: FLYING");
    }

    @Test
    @DisplayName("TC-11: Sales report returns correct revenue data")
    void getSalesReport_ReturnsCorrectData() {
        when(orderRepository.totalRevenue()).thenReturn(500.0);
        when(orderRepository.countByStatus(OrderStatus.DELIVERED)).thenReturn(5L);
        when(orderRepository.count()).thenReturn(8L);
        when(orderRepository.countByStatus(OrderStatus.CUSTOMER_CANCELLED)).thenReturn(1L);
        when(orderRepository.countByStatus(OrderStatus.ADMIN_CANCELLED)).thenReturn(0L);

        Map<String, Object> report = adminService.getSalesReport();

        assertThat(report)
            .containsEntry("totalRevenue", 500.0)
            .containsEntry("deliveredOrders", 5L)
            .containsEntry("averageOrderValue", 100.0)
            .containsEntry("totalOrders", 8L);
    }
}
