package com.pharmacy.catalog.controller;

import com.pharmacy.catalog.dto.MedicineRequest;
import com.pharmacy.catalog.entity.Medicine;
import com.pharmacy.catalog.service.MedicineService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/catalog/medicines")
@Tag(name = "Medicine Catalog", description = "Browse and search medicines")
public class MedicineController {

    @Autowired
    private MedicineService medicineService;

    // ── PUBLIC ──────────────────────────────────────────────────────────

    @GetMapping
    @Operation(summary = "Get all medicines (public)", description = "Search by keyword or filter by category")
    public ResponseEntity<List<Medicine>> getAllMedicines(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String category) {
        if (search != null && !search.isEmpty())
            return ResponseEntity.ok(medicineService.searchMedicines(search));
        if (category != null && !category.isEmpty())
            return ResponseEntity.ok(medicineService.getMedicinesByCategory(category));
        return ResponseEntity.ok(medicineService.getAllMedicines());
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get medicine by ID (public)")
    public ResponseEntity<Medicine> getMedicineById(@PathVariable Long id) {
        return ResponseEntity.ok(medicineService.getMedicineById(id));
    }

    // ── ADMIN ONLY ──────────────────────────────────────────────────────

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Add medicine [ADMIN]", security = @SecurityRequirement(name = "Bearer Authentication"))
    public ResponseEntity<Medicine> addMedicine(@Valid @RequestBody MedicineRequest request) {
        return ResponseEntity.ok(medicineService.addMedicine(request));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Update medicine [ADMIN]", security = @SecurityRequirement(name = "Bearer Authentication"))
    public ResponseEntity<Medicine> updateMedicine(@PathVariable Long id, @Valid @RequestBody MedicineRequest request) {
        return ResponseEntity.ok(medicineService.updateMedicine(id, request));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Deactivate medicine [ADMIN]", security = @SecurityRequirement(name = "Bearer Authentication"))
    public ResponseEntity<String> deleteMedicine(@PathVariable Long id) {
        medicineService.deleteMedicine(id);
        return ResponseEntity.ok("Medicine deactivated successfully");
    }
}
