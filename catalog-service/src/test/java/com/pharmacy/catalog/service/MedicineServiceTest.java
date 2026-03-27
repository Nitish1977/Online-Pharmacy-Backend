package com.pharmacy.catalog.service;

import com.pharmacy.catalog.dto.MedicineRequest;
import com.pharmacy.catalog.entity.Medicine;
import com.pharmacy.catalog.enums.MedicineStatus;
import com.pharmacy.catalog.repository.MedicineRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("MedicineService Unit Tests")
class MedicineServiceTest {

    @Mock
    private MedicineRepository medicineRepository;

    @InjectMocks
    private MedicineService medicineService;

    private Medicine mockMedicine;

    @BeforeEach
    void setUp() {
        mockMedicine = new Medicine();
        mockMedicine.setId(1L);
        mockMedicine.setName("Paracetamol 500mg");
        mockMedicine.setBrand("Calpol");
        mockMedicine.setCategory("Pain Relief");
        mockMedicine.setPrice(25.50);
        mockMedicine.setStock(100);
        mockMedicine.setRequiresPrescription(false);
        mockMedicine.setExpiryDate(LocalDate.of(2026, 12, 31));
        mockMedicine.setStatus(MedicineStatus.ACTIVE);
    }

    @Test
    @DisplayName("TC-01: Get all medicines returns active medicines list")
    void getAllMedicines_ReturnsActiveMedicinesList() {
        when(medicineRepository.findByStatus(MedicineStatus.ACTIVE))
                .thenReturn(List.of(mockMedicine));

        List<Medicine> result = medicineService.getAllMedicines();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getName()).isEqualTo("Paracetamol 500mg");
        verify(medicineRepository).findByStatus(MedicineStatus.ACTIVE);
    }

    @Test
    @DisplayName("TC-02: Get medicine by valid ID returns medicine")
    void getMedicineById_ValidId_ReturnsMedicine() {
        when(medicineRepository.findById(1L)).thenReturn(Optional.of(mockMedicine));

        Medicine result = medicineService.getMedicineById(1L);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getName()).isEqualTo("Paracetamol 500mg");
    }

    @Test
    @DisplayName("TC-03: Get medicine by invalid ID throws exception")
    void getMedicineById_InvalidId_ThrowsException() {
        when(medicineRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> medicineService.getMedicineById(99L))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Medicine not found with id: 99");
    }

    @Test
    @DisplayName("TC-04: Search medicines by keyword returns matching results")
    void searchMedicines_WithKeyword_ReturnsMatches() {
        when(medicineRepository.searchMedicines("Paracetamol"))
                .thenReturn(List.of(mockMedicine));

        List<Medicine> result = medicineService.searchMedicines("Paracetamol");

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getName()).contains("Paracetamol");
    }

    @Test
    @DisplayName("TC-05: Search with empty keyword returns all medicines")
    void searchMedicines_EmptyKeyword_ReturnsAll() {
        when(medicineRepository.findByStatus(MedicineStatus.ACTIVE))
                .thenReturn(List.of(mockMedicine));

        List<Medicine> result = medicineService.searchMedicines("");

        assertThat(result).hasSize(1);
        verify(medicineRepository, never()).searchMedicines(any());
    }

    @Test
    @DisplayName("TC-06: Add medicine saves and returns new medicine")
    void addMedicine_ValidRequest_SavesAndReturns() {
        MedicineRequest request = new MedicineRequest();
        request.setName("Amoxicillin 500mg");
        request.setBrand("Novamox");
        request.setCategory("Antibiotic");
        request.setPrice(85.00);
        request.setStock(50);
        request.setRequiresPrescription(true);
        request.setExpiryDate(LocalDate.of(2025, 10, 31));

        Medicine saved = new Medicine();
        saved.setId(2L);
        saved.setName("Amoxicillin 500mg");
        saved.setRequiresPrescription(true);

        when(medicineRepository.save(any(Medicine.class))).thenReturn(saved);

        Medicine result = medicineService.addMedicine(request);

        assertThat(result.getId()).isEqualTo(2L);
        assertThat(result.getName()).isEqualTo("Amoxicillin 500mg");
        assertThat(result.isRequiresPrescription()).isTrue();
        verify(medicineRepository).save(any(Medicine.class));
    }

    @Test
    @DisplayName("TC-07: Update medicine updates fields and saves")
    void updateMedicine_ValidRequest_UpdatesAndSaves() {
        MedicineRequest request = new MedicineRequest();
        request.setName("Paracetamol 500mg Updated");
        request.setPrice(30.00);
        request.setStock(150);
        request.setCategory("Pain Relief");
        request.setRequiresPrescription(false);

        Medicine updated = new Medicine();
        updated.setId(1L);
        updated.setName("Paracetamol 500mg Updated");
        updated.setPrice(30.00);

        when(medicineRepository.findById(1L)).thenReturn(Optional.of(mockMedicine));
        when(medicineRepository.save(any(Medicine.class))).thenReturn(updated);

        Medicine result = medicineService.updateMedicine(1L, request);

        assertThat(result.getName()).isEqualTo("Paracetamol 500mg Updated");
        verify(medicineRepository).save(any(Medicine.class));
    }

    @Test
    @DisplayName("TC-08: Delete medicine sets status to INACTIVE")
    void deleteMedicine_ValidId_SetsInactive() {
        when(medicineRepository.findById(1L)).thenReturn(Optional.of(mockMedicine));
        when(medicineRepository.save(any(Medicine.class))).thenReturn(mockMedicine);

        medicineService.deleteMedicine(1L);

        verify(medicineRepository).save(argThat(m -> m.getStatus() == MedicineStatus.INACTIVE));
    }
}
