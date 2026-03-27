package com.pharmacy.admin.repository.catalog;

import com.pharmacy.admin.entity.catalog.MedicineEntity;
import com.pharmacy.admin.enums.MedicineStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface MedicineEntityRepository extends JpaRepository<MedicineEntity, Long> {

    List<MedicineEntity> findByStatus(MedicineStatus status);

    List<MedicineEntity> findByStockLessThan(int threshold);

    @Query("SELECT m FROM MedicineEntity m WHERE m.expiryDate <= :date AND m.status = 'ACTIVE'")
    List<MedicineEntity> findExpiringBefore(@Param("date") LocalDate date);

    @Query("SELECT COUNT(m) FROM MedicineEntity m WHERE m.status = 'ACTIVE'")
    long countActiveMedicines();
}
