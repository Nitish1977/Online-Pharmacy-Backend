package com.pharmacy.catalog.repository;

import com.pharmacy.catalog.entity.Prescription;
import com.pharmacy.catalog.enums.PrescriptionStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PrescriptionRepository extends JpaRepository<Prescription, Long> {
    List<Prescription> findByCustomerId(Long customerId);
    List<Prescription> findByStatus(PrescriptionStatus status);
}
