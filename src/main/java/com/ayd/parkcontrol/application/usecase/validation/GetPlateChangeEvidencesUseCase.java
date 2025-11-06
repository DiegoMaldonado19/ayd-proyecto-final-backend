package com.ayd.parkcontrol.application.usecase.validation;

import com.ayd.parkcontrol.application.dto.response.validation.EvidenceResponse;
import com.ayd.parkcontrol.application.mapper.PlateChangeRequestDtoMapper;
import com.ayd.parkcontrol.domain.model.validation.ChangeRequestEvidence;
import com.ayd.parkcontrol.domain.repository.ChangeRequestEvidenceRepository;
import com.ayd.parkcontrol.infrastructure.persistence.entity.DocumentTypeEntity;
import com.ayd.parkcontrol.infrastructure.persistence.repository.JpaDocumentTypeRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class GetPlateChangeEvidencesUseCase {

    private final ChangeRequestEvidenceRepository evidenceRepository;
    private final JpaDocumentTypeRepository documentTypeRepository;
    private final PlateChangeRequestDtoMapper mapper;

    @Transactional(readOnly = true)
    public List<EvidenceResponse> execute(Long requestId) {
        log.info("Fetching evidences for plate change request ID: {}", requestId);

        List<ChangeRequestEvidence> evidences = evidenceRepository.findByChangeRequestId(requestId);

        return evidences.stream()
                .map(evidence -> {
                    DocumentTypeEntity documentType = documentTypeRepository
                            .findById(evidence.getDocumentTypeId())
                            .orElse(null);

                    String documentTypeCode = documentType != null ? documentType.getCode() : null;
                    String documentTypeName = documentType != null ? documentType.getName() : null;

                    return mapper.toEvidenceResponse(evidence, documentTypeCode, documentTypeName);
                })
                .collect(Collectors.toList());
    }
}
