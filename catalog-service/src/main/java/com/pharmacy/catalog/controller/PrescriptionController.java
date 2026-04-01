package com.pharmacy.catalog.controller;

import com.pharmacy.catalog.entity.Prescription;
import com.pharmacy.catalog.service.PrescriptionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/api/catalog/prescriptions")
@Tag(name = "Prescription Management", description = "Upload and manage prescriptions")
@SecurityRequirement(name = "Bearer Authentication")
public class PrescriptionController {

    private final PrescriptionService prescriptionService;

    public PrescriptionController(PrescriptionService prescriptionService) {
        this.prescriptionService = prescriptionService;
    }

    // ── CUSTOMER ────────────────────────────────────────────────────────

    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasRole('CUSTOMER') or hasRole('ADMIN')")
    @Operation(summary = "Upload prescription [CUSTOMER/ADMIN]", description = "Accepts PDF, JPG, PNG (max 10MB)")
    public ResponseEntity<Prescription> uploadPrescription(
            @RequestParam("customerId") Long customerId,
            @RequestPart("file") MultipartFile file) throws IOException {
        return ResponseEntity.ok(prescriptionService.uploadPrescription(customerId, file));
    }

    @GetMapping("/my/{customerId}")
    @PreAuthorize("hasRole('CUSTOMER') or hasRole('ADMIN')")
    @Operation(summary = "Get my prescriptions [CUSTOMER/ADMIN]")
    public ResponseEntity<List<Prescription>> getMyPrescriptions(@PathVariable Long customerId) {
        return ResponseEntity.ok(prescriptionService.getMyPrescriptions(customerId));
    }

    // ── ADMIN ONLY ──────────────────────────────────────────────────────

    @GetMapping("/pending")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Get all pending prescriptions [ADMIN]")
    public ResponseEntity<List<Prescription>> getPendingPrescriptions() {
        return ResponseEntity.ok(prescriptionService.getPendingPrescriptions());
    }

    @PutMapping("/{id}/status")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Approve or reject prescription [ADMIN]")
    public ResponseEntity<Prescription> updatePrescriptionStatus(
            @PathVariable Long id,
            @RequestParam String status,
            @RequestParam(required = false) String remarks) {
        return ResponseEntity.ok(prescriptionService.updateStatus(id, status, remarks));
    }
}
