package com.pharmacy.order.service;

import com.pharmacy.order.dto.CartRequest;
import com.pharmacy.order.dto.CheckoutRequest;
import com.pharmacy.order.entity.CartItem;
import com.pharmacy.order.entity.Order;
import com.pharmacy.order.entity.OrderItem;
import com.pharmacy.order.enums.OrderStatus;
import com.pharmacy.order.repository.CartItemRepository;
import com.pharmacy.order.repository.OrderRepository;
import com.pharmacy.order.config.RabbitMQConfig;
import com.pharmacy.order.dto.OrderPlacedEvent;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class OrderService {

    @Autowired private OrderRepository orderRepository;
    @Autowired private CartItemRepository cartItemRepository;
    @Autowired private RabbitTemplate rabbitTemplate;

    // ─── Cart ───────────────────────────────────────────────────────────

    public CartItem addToCart(CartRequest request) {
        Optional<CartItem> existing = cartItemRepository
                .findByCustomerIdAndMedicineId(request.getCustomerId(), request.getMedicineId());
        if (existing.isPresent()) {
            CartItem item = existing.get();
            item.setQuantity(item.getQuantity() + request.getQuantity());
            return cartItemRepository.save(item);
        }
        CartItem item = new CartItem();
        item.setCustomerId(request.getCustomerId());
        item.setMedicineId(request.getMedicineId());
        item.setMedicineName(request.getMedicineName());
        item.setQuantity(request.getQuantity());
        item.setUnitPrice(request.getUnitPrice());
        item.setRequiresPrescription(request.isRequiresPrescription());
        return cartItemRepository.save(item);
    }

    public List<CartItem> getCart(Long customerId) {
        return cartItemRepository.findByCustomerId(customerId);
    }

    public CartItem updateCartItem(Long cartItemId, int quantity) {
        CartItem item = cartItemRepository.findById(cartItemId)
                .orElseThrow(() -> new RuntimeException("Cart item not found"));
        if (quantity <= 0) { cartItemRepository.delete(item); return null; }
        item.setQuantity(quantity);
        return cartItemRepository.save(item);
    }

    @Transactional
    public void removeFromCart(Long cartItemId) {
        cartItemRepository.deleteById(cartItemId);
    }

    @Transactional
    public void clearCart(Long customerId) {
        cartItemRepository.deleteByCustomerId(customerId);
    }

    // ─── Checkout ───────────────────────────────────────────────────────

    @Transactional
    public Order checkout(CheckoutRequest request) {
        List<CartItem> cartItems = cartItemRepository.findByCustomerId(request.getCustomerId());
        if (cartItems.isEmpty()) throw new RuntimeException("Cart is empty");

        boolean needsPrescription = cartItems.stream().anyMatch(CartItem::isRequiresPrescription);

        Order order = new Order();
        order.setCustomerId(request.getCustomerId());
        order.setDeliveryAddress(request.getDeliveryAddress());
        order.setPincode(request.getPincode());
        order.setDeliverySlot(request.getDeliverySlot());
        order.setPrescriptionId(request.getPrescriptionId());

        double total = 0;
        for (CartItem cartItem : cartItems) {
            OrderItem orderItem = new OrderItem();
            orderItem.setOrder(order);
            orderItem.setMedicineId(cartItem.getMedicineId());
            orderItem.setMedicineName(cartItem.getMedicineName());
            orderItem.setQuantity(cartItem.getQuantity());
            orderItem.setUnitPrice(cartItem.getUnitPrice());
            orderItem.setSubtotal(cartItem.getUnitPrice() * cartItem.getQuantity());
            total += orderItem.getSubtotal();
            order.getItems().add(orderItem);
        }
        order.setTotalAmount(total);
        order.setStatus(needsPrescription && request.getPrescriptionId() == null
                ? OrderStatus.PRESCRIPTION_PENDING
                : OrderStatus.PAYMENT_PENDING);

        Order saved = orderRepository.save(order);
        cartItemRepository.deleteByCustomerId(request.getCustomerId());

        // Publish OrderPlacedEvent
        try {
            OrderPlacedEvent event = new OrderPlacedEvent();
            event.setOrderId(saved.getId());
            List<OrderPlacedEvent.OrderItemDto> dtoList = saved.getItems().stream()
                    .map(item -> new OrderPlacedEvent.OrderItemDto(item.getMedicineId(), item.getQuantity()))
                    .toList();
            event.setItems(dtoList);
            rabbitTemplate.convertAndSend(RabbitMQConfig.EXCHANGE, RabbitMQConfig.ROUTING_KEY_ORDER_PLACED, event);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return saved;
    }

    // ─── Payment ────────────────────────────────────────────────────────

    public Order initiatePayment(Long orderId) {
        Order order = getOrderById(orderId);
        if (order.getStatus() != OrderStatus.PAYMENT_PENDING)
            throw new RuntimeException("Order is not in PAYMENT_PENDING state");
        order.setPaymentId("PAY-" + System.currentTimeMillis());
        order.setStatus(OrderStatus.PAID);
        order.setUpdatedAt(LocalDateTime.now());
        return orderRepository.save(order);
    }

    // ─── Orders ─────────────────────────────────────────────────────────

    public List<Order> getMyOrders(Long customerId) {
        return orderRepository.findByCustomerIdOrderByCreatedAtDesc(customerId);
    }

    public Order getOrderById(Long orderId) {
        return orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found: " + orderId));
    }

    public Order cancelOrder(Long orderId) {
        Order order = getOrderById(orderId);
        if (order.getStatus() == OrderStatus.PACKED ||
            order.getStatus() == OrderStatus.OUT_FOR_DELIVERY ||
            order.getStatus() == OrderStatus.DELIVERED)
            throw new RuntimeException("Order cannot be cancelled at this stage");
        order.setStatus(OrderStatus.CUSTOMER_CANCELLED);
        order.setUpdatedAt(LocalDateTime.now());
        return orderRepository.save(order);
    }
}
