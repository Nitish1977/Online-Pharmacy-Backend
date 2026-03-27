package com.pharmacy.catalog.service;

import com.pharmacy.catalog.entity.Prescription;
import com.pharmacy.catalog.enums.PrescriptionStatus;
import com.pharmacy.catalog.repository.PrescriptionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
public class PrescriptionService {

    @Autowired
    private PrescriptionRepository prescriptionRepository;

    @Value("${prescription.upload.dir}")
    private String uploadDir;

    public Prescription uploadPrescription(Long customerId, MultipartFile file) throws IOException {
        String contentType = file.getContentType();
        if (contentType == null ||
            (!contentType.equals("application/pdf") &&
             !contentType.equals("image/jpeg") &&
             !contentType.equals("image/png"))) {
            throw new RuntimeException("Only PDF, JPG, or PNG files are allowed");
        }
        Path uploadPath = Paths.get(uploadDir);
        if (!Files.exists(uploadPath)) Files.createDirectories(uploadPath);
        String uniqueFileName = UUID.randomUUID() + "_" + file.getOriginalFilename();
        Path filePath = uploadPath.resolve(uniqueFileName);
        Files.copy(file.getInputStream(), filePath);

        Prescription prescription = new Prescription();
        prescription.setCustomerId(customerId);
        prescription.setFileName(file.getOriginalFilename());
        prescription.setFilePath(filePath.toString());
        prescription.setFileType(contentType);
        prescription.setStatus(PrescriptionStatus.PENDING);
        return prescriptionRepository.save(prescription);
    }

    public List<Prescription> getMyPrescriptions(Long customerId) {
        return prescriptionRepository.findByCustomerId(customerId);
    }

    public List<Prescription> getPendingPrescriptions() {
        return prescriptionRepository.findByStatus(PrescriptionStatus.PENDING);
    }

    public Prescription updateStatus(Long id, String status, String remarks) {
        Prescription prescription = prescriptionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Prescription not found"));
        prescription.setStatus(PrescriptionStatus.valueOf(status.toUpperCase()));
        prescription.setRemarks(remarks);
        prescription.setVerifiedAt(LocalDateTime.now());
        return prescriptionRepository.save(prescription);
    }
}
