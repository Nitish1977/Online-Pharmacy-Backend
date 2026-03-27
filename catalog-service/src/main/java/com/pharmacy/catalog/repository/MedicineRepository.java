package com.pharmacy.catalog.repository;

import com.pharmacy.catalog.entity.Medicine;
import com.pharmacy.catalog.enums.MedicineStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MedicineRepository extends JpaRepository<Medicine, Long> {

    @Query("SELECT m FROM Medicine m WHERE " +
           "LOWER(m.name) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(m.brand) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(m.category) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    List<Medicine> searchMedicines(@Param("keyword") String keyword);

    List<Medicine> findByCategory(String category);
    List<Medicine> findByRequiresPrescription(boolean requiresPrescription);
    List<Medicine> findByStatus(MedicineStatus status);
}
