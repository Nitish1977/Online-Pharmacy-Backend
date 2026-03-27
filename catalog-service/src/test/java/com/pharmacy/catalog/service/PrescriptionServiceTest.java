package com.pharmacy.catalog.service;

import com.pharmacy.catalog.entity.Prescription;
import com.pharmacy.catalog.enums.PrescriptionStatus;
import com.pharmacy.catalog.repository.PrescriptionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("PrescriptionService Unit Tests")
class PrescriptionServiceTest {

    @Mock
    private PrescriptionRepository prescriptionRepository;

    @InjectMocks
    private PrescriptionService prescriptionService;

    private Prescription mockPrescription;

    @BeforeEach
    void setUp() {
        // Set upload dir via reflection since @Value won't be injected in unit tests
        try {
            java.lang.reflect.Field f = PrescriptionService.class.getDeclaredField("uploadDir");
            f.setAccessible(true);
            f.set(prescriptionService, System.getProperty("java.io.tmpdir") + "/test-prescriptions");
        } catch (Exception e) {
            // ignore for test
        }

        mockPrescription = new Prescription();
        mockPrescription.setId(1L);
        mockPrescription.setCustomerId(1L);
        mockPrescription.setFileName("prescription.pdf");
        mockPrescription.setStatus(PrescriptionStatus.PENDING);
    }

    @Test
    @DisplayName("TC-09: Upload valid PDF prescription saves and returns PENDING status")
    void uploadPrescription_ValidPdf_ReturnsPendingPrescription() throws IOException {
        MockMultipartFile file = new MockMultipartFile(
                "file", "prescription.pdf",
                "application/pdf", "pdf content".getBytes());

        when(prescriptionRepository.save(any(Prescription.class))).thenReturn(mockPrescription);

        Prescription result = prescriptionService.uploadPrescription(1L, file);

        assertThat(result).isNotNull();
        assertThat(result.getStatus()).isEqualTo(PrescriptionStatus.PENDING);
        assertThat(result.getCustomerId()).isEqualTo(1L);
        verify(prescriptionRepository).save(any(Prescription.class));
    }

    @Test
    @DisplayName("TC-10: Upload invalid file type throws exception")
    void uploadPrescription_InvalidFileType_ThrowsException() {
        MockMultipartFile file = new MockMultipartFile(
                "file", "document.txt",
                "text/plain", "text content".getBytes());

        assertThatThrownBy(() -> prescriptionService.uploadPrescription(1L, file))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Only PDF, JPG, or PNG files are allowed");

        verify(prescriptionRepository, never()).save(any());
    }

    @Test
    @DisplayName("TC-11: Get my prescriptions returns prescriptions for customer")
    void getMyPrescriptions_ValidCustomerId_ReturnsList() {
        when(prescriptionRepository.findByCustomerId(1L))
                .thenReturn(List.of(mockPrescription));

        List<Prescription> result = prescriptionService.getMyPrescriptions(1L);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getCustomerId()).isEqualTo(1L);
    }

    @Test
    @DisplayName("TC-12: Approve prescription updates status to APPROVED")
    void updateStatus_Approved_SetsApprovedStatus() {
        when(prescriptionRepository.findById(1L)).thenReturn(Optional.of(mockPrescription));
        when(prescriptionRepository.save(any(Prescription.class))).thenReturn(mockPrescription);

        Prescription result = prescriptionService.updateStatus(1L, "APPROVED", "Valid");

        verify(prescriptionRepository).save(argThat(
                p -> p.getStatus() == PrescriptionStatus.APPROVED
                  && "Valid".equals(p.getRemarks())
        ));
    }

    @Test
    @DisplayName("TC-13: Reject prescription updates status to REJECTED")
    void updateStatus_Rejected_SetsRejectedStatus() {
        when(prescriptionRepository.findById(1L)).thenReturn(Optional.of(mockPrescription));
        when(prescriptionRepository.save(any(Prescription.class))).thenReturn(mockPrescription);

        prescriptionService.updateStatus(1L, "REJECTED", "Illegible");

        verify(prescriptionRepository).save(argThat(
                p -> p.getStatus() == PrescriptionStatus.REJECTED
        ));
    }

    @Test
    @DisplayName("TC-14: Update status for non-existent prescription throws exception")
    void updateStatus_NonExistentId_ThrowsException() {
        when(prescriptionRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> prescriptionService.updateStatus(99L, "APPROVED", ""))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Prescription not found");
    }

    @Test
    @DisplayName("TC-15: Get pending prescriptions returns only PENDING ones")
    void getPendingPrescriptions_ReturnsPendingOnly() {
        when(prescriptionRepository.findByStatus(PrescriptionStatus.PENDING))
                .thenReturn(List.of(mockPrescription));

        List<Prescription> result = prescriptionService.getPendingPrescriptions();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getStatus()).isEqualTo(PrescriptionStatus.PENDING);
    }
}
