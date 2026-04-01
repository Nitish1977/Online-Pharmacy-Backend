package com.pharmacy.order.service;

import com.pharmacy.order.dto.CartRequest;
import com.pharmacy.order.dto.CheckoutRequest;
import com.pharmacy.order.entity.CartItem;
import com.pharmacy.order.entity.Order;
import com.pharmacy.order.enums.OrderStatus;
import com.pharmacy.order.repository.CartItemRepository;
import com.pharmacy.order.repository.OrderRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.springframework.amqp.rabbit.core.RabbitTemplate;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("OrderService Unit Tests")
class OrderServiceTest {

    @Mock
    private OrderRepository orderRepository;
    @Mock
    private CartItemRepository cartItemRepository;
    @Mock
    private RabbitTemplate rabbitTemplate;

    @InjectMocks
    private OrderService orderService;

    private CartItem mockCartItem;
    private Order mockOrder;

    @BeforeEach
    void setUp() {
        mockCartItem = new CartItem();
        mockCartItem.setId(1L);
        mockCartItem.setCustomerId(1L);
        mockCartItem.setMedicineId(1L);
        mockCartItem.setMedicineName("Paracetamol 500mg");
        mockCartItem.setQuantity(2);
        mockCartItem.setUnitPrice(25.50);
        mockCartItem.setRequiresPrescription(false);

        mockOrder = new Order();
        mockOrder.setId(1L);
        mockOrder.setCustomerId(1L);
        mockOrder.setStatus(OrderStatus.PAYMENT_PENDING);
        mockOrder.setTotalAmount(51.00);
        mockOrder.setItems(new ArrayList<>());
    }

    // ── Cart Tests ───────────────────────────────────────────────────────────

    @Test
    @DisplayName("TC-01: Add new item to empty cart creates cart item")
    void addToCart_NewItem_CreatesCartItem() {
        CartRequest request = new CartRequest();
        request.setCustomerId(1L);
        request.setMedicineId(1L);
        request.setMedicineName("Paracetamol 500mg");
        request.setQuantity(2);
        request.setUnitPrice(25.50);
        request.setRequiresPrescription(false);

        when(cartItemRepository.findByCustomerIdAndMedicineId(1L, 1L))
                .thenReturn(Optional.empty());
        when(cartItemRepository.save(any(CartItem.class))).thenReturn(mockCartItem);

        CartItem result = orderService.addToCart(request);

        assertThat(result).isNotNull();
        assertThat(result.getMedicineName()).isEqualTo("Paracetamol 500mg");
        verify(cartItemRepository).save(any(CartItem.class));
    }

    @Test
    @DisplayName("TC-02: Add existing item to cart increments quantity")
    void addToCart_ExistingItem_IncrementsQuantity() {
        CartRequest request = new CartRequest();
        request.setCustomerId(1L);
        request.setMedicineId(1L);
        request.setQuantity(3);

        when(cartItemRepository.findByCustomerIdAndMedicineId(1L, 1L))
                .thenReturn(Optional.of(mockCartItem));
        when(cartItemRepository.save(any(CartItem.class))).thenReturn(mockCartItem);

        orderService.addToCart(request);

        // Original quantity was 2, adding 3 = 5
        verify(cartItemRepository).save(argThat(c -> c.getQuantity() == 5));
    }

    @Test
    @DisplayName("TC-03: Get cart returns all items for customer")
    void getCart_ValidCustomer_ReturnsItems() {
        when(cartItemRepository.findByCustomerId(1L)).thenReturn(List.of(mockCartItem));

        List<CartItem> result = orderService.getCart(1L);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getCustomerId()).isEqualTo(1L);
    }

    @Test
    @DisplayName("TC-04: Update cart item with quantity 0 removes item")
    void updateCartItem_ZeroQuantity_RemovesItem() {
        when(cartItemRepository.findById(1L)).thenReturn(Optional.of(mockCartItem));

        CartItem result = orderService.updateCartItem(1L, 0);

        assertThat(result).isNull();
        verify(cartItemRepository).delete(mockCartItem);
    }

    @Test
    @DisplayName("TC-05: Update cart item with positive quantity updates it")
    void updateCartItem_PositiveQuantity_UpdatesItem() {
        when(cartItemRepository.findById(1L)).thenReturn(Optional.of(mockCartItem));
        when(cartItemRepository.save(any(CartItem.class))).thenReturn(mockCartItem);

        orderService.updateCartItem(1L, 5);

        verify(cartItemRepository).save(argThat(c -> c.getQuantity() == 5));
    }

    // ── Checkout Tests ───────────────────────────────────────────────────────

    @Test
    @DisplayName("TC-06: Checkout with no Rx medicines creates PAYMENT_PENDING order")
    void checkout_NoRxMedicines_CreatesPaymentPendingOrder() {
        CheckoutRequest request = new CheckoutRequest();
        request.setCustomerId(1L);
        request.setDeliveryAddress("45 MG Road, Ludhiana");
        request.setPincode("141001");
        request.setDeliverySlot("10AM-12PM");

        when(cartItemRepository.findByCustomerId(1L)).thenReturn(List.of(mockCartItem));
        when(orderRepository.save(any(Order.class))).thenReturn(mockOrder);

        Order result = orderService.checkout(request);

        assertThat(result).isNotNull();
        assertThat(result.getStatus()).isEqualTo(OrderStatus.PAYMENT_PENDING);
        verify(cartItemRepository).deleteByCustomerId(1L);
    }

    @Test
    @DisplayName("TC-07: Checkout with Rx medicine and no prescriptionId sets PRESCRIPTION_PENDING")
    void checkout_RxMedicineNoPrescription_SetsPrescriptionPending() {
        mockCartItem.setRequiresPrescription(true);

        CheckoutRequest request = new CheckoutRequest();
        request.setCustomerId(1L);
        request.setDeliveryAddress("45 MG Road");
        request.setPincode("141001");
        // No prescriptionId set

        Order rxPendingOrder = new Order();
        rxPendingOrder.setId(2L);
        rxPendingOrder.setStatus(OrderStatus.PRESCRIPTION_PENDING);
        rxPendingOrder.setItems(new ArrayList<>());

        when(cartItemRepository.findByCustomerId(1L)).thenReturn(List.of(mockCartItem));
        when(orderRepository.save(any(Order.class))).thenReturn(rxPendingOrder);

        Order result = orderService.checkout(request);

        assertThat(result.getStatus()).isEqualTo(OrderStatus.PRESCRIPTION_PENDING);
    }

    @Test
    @DisplayName("TC-08: Checkout with empty cart throws exception")
    void checkout_EmptyCart_ThrowsException() {
        CheckoutRequest request = new CheckoutRequest();
        request.setCustomerId(1L);
        request.setDeliveryAddress("45 MG Road");
        request.setPincode("141001");

        when(cartItemRepository.findByCustomerId(1L)).thenReturn(List.of());

        assertThatThrownBy(() -> orderService.checkout(request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Cart is empty");

        verify(orderRepository, never()).save(any());
    }

    // ── Payment Tests ────────────────────────────────────────────────────────

    @Test
    @DisplayName("TC-09: Initiate payment on PAYMENT_PENDING order sets PAID status")
    void initiatePayment_PaymentPendingOrder_SetsOrderPaid() {
        mockOrder.setStatus(OrderStatus.PAYMENT_PENDING);

        Order paidOrder = new Order();
        paidOrder.setId(1L);
        paidOrder.setStatus(OrderStatus.PAID);
        paidOrder.setPaymentId("PAY-123");

        when(orderRepository.findById(1L)).thenReturn(Optional.of(mockOrder));
        when(orderRepository.save(any(Order.class))).thenReturn(paidOrder);

        Order result = orderService.initiatePayment(1L);

        assertThat(result.getStatus()).isEqualTo(OrderStatus.PAID);
        verify(orderRepository).save(argThat(o -> o.getStatus() == OrderStatus.PAID && o.getPaymentId() != null));
    }

    @Test
    @DisplayName("TC-10: Initiate payment on already PAID order throws exception")
    void initiatePayment_AlreadyPaidOrder_ThrowsException() {
        mockOrder.setStatus(OrderStatus.PAID);

        when(orderRepository.findById(1L)).thenReturn(Optional.of(mockOrder));

        assertThatThrownBy(() -> orderService.initiatePayment(1L))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("Order is not in PAYMENT_PENDING state");
    }

    @Test
    @DisplayName("TC-11: Cancel order in PAYMENT_PENDING state sets CUSTOMER_CANCELLED")
    void cancelOrder_PaymentPendingStatus_SetsCustomerCancelled() {
        mockOrder.setStatus(OrderStatus.PAYMENT_PENDING);

        Order cancelledOrder = new Order();
        cancelledOrder.setId(1L);
        cancelledOrder.setStatus(OrderStatus.CUSTOMER_CANCELLED);

        when(orderRepository.findById(1L)).thenReturn(Optional.of(mockOrder));
        when(orderRepository.save(any(Order.class))).thenReturn(cancelledOrder);

        Order result = orderService.cancelOrder(1L);

        assertThat(result.getStatus()).isEqualTo(OrderStatus.CUSTOMER_CANCELLED);
    }

    @Test
    @DisplayName("TC-12: Cancel order in DELIVERED state throws exception")
    void cancelOrder_DeliveredStatus_ThrowsException() {
        mockOrder.setStatus(OrderStatus.DELIVERED);

        when(orderRepository.findById(1L)).thenReturn(Optional.of(mockOrder));

        assertThatThrownBy(() -> orderService.cancelOrder(1L))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("Order cannot be cancelled at this stage");
    }

    @Test
    @DisplayName("TC-13: Get my orders returns orders sorted by date")
    void getMyOrders_ValidCustomer_ReturnsOrderList() {
        when(orderRepository.findByCustomerIdOrderByCreatedAtDesc(1L))
                .thenReturn(List.of(mockOrder));

        List<Order> result = orderService.getMyOrders(1L);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getCustomerId()).isEqualTo(1L);
    }
}
