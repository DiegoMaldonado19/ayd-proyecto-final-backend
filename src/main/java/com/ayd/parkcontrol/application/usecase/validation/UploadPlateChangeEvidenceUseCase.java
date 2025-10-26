package com.ayd.parkcontrol.application.usecase.validation;

import com.ayd.parkcontrol.application.dto.response.validation.EvidenceResponse;
import com.ayd.parkcontrol.application.mapper.PlateChangeRequestDtoMapper;
import com.ayd.parkcontrol.application.port.output.BlobStorageService;
import com.ayd.parkcontrol.domain.exception.PlateChangeRequestNotFoundException;
import com.ayd.parkcontrol.domain.model.validation.ChangeRequestEvidence;
import com.ayd.parkcontrol.domain.repository.ChangeRequestEvidenceRepository;
import com.ayd.parkcontrol.domain.repository.PlateChangeRequestRepository;
import com.ayd.parkcontrol.infrastructure.persistence.repository.JpaDocumentTypeRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class UploadPlateChangeEvidenceUseCase {

    private final PlateChangeRequestRepository plateChangeRequestRepository;
    private final ChangeRequestEvidenceRepository evidenceRepository;
    private final JpaDocumentTypeRepository documentTypeRepository;
    private final BlobStorageService blobStorageService;
    private final PlateChangeRequestDtoMapper mapper;

    @Value("${azure.storage.container.plate-changes}")
    private String containerName;

    @Transactional
    public EvidenceResponse execute(Long requestId, Integer documentTypeId, MultipartFile file) {
        log.info("Uploading evidence for plate change request: {}", requestId);

        plateChangeRequestRepository.findById(requestId)
                .orElseThrow(() -> new PlateChangeRequestNotFoundException(requestId));

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String uploaderEmail = authentication.getName();

        String originalFilename = file.getOriginalFilename();
        String extension = originalFilename != null && originalFilename.contains(".")
                ? originalFilename.substring(originalFilename.lastIndexOf("."))
                : "";
        String blobName = String.format("request-%d/%s%s", requestId, UUID.randomUUID(), extension);

        String fileUrl = blobStorageService.uploadFile(containerName, file, blobName);

        ChangeRequestEvidence evidence = ChangeRequestEvidence.builder()
                .changeRequestId(requestId)
                .documentTypeId(documentTypeId)
                .fileName(originalFilename)
                .fileUrl(fileUrl)
                .fileSize(file.getSize())
                .uploadedBy(uploaderEmail)
                .build();

        ChangeRequestEvidence saved = evidenceRepository.save(evidence);

        var documentType = documentTypeRepository.findById(documentTypeId).orElse(null);
        String documentTypeCode = documentType != null ? documentType.getCode() : null;
        String documentTypeName = documentType != null ? documentType.getName() : null;

        log.info("Evidence uploaded successfully with ID: {}", saved.getId());

        return mapper.toEvidenceResponse(saved, documentTypeCode, documentTypeName);
    }
}
