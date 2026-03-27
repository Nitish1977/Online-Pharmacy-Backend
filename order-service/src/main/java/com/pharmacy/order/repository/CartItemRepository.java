package com.pharmacy.order.repository;

import com.pharmacy.order.entity.CartItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CartItemRepository extends JpaRepository<CartItem, Long> {
    List<CartItem> findByCustomerId(Long customerId);
    Optional<CartItem> findByCustomerIdAndMedicineId(Long customerId, Long medicineId);
    void deleteByCustomerId(Long customerId);
}
