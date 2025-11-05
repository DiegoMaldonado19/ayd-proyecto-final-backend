package com.ayd.parkcontrol.application.usecase.validation;

import com.ayd.parkcontrol.application.dto.response.validation.EvidenceResponse;
import com.ayd.parkcontrol.application.dto.response.validation.PlateChangeRequestResponse;
import com.ayd.parkcontrol.application.mapper.PlateChangeRequestDtoMapper;
import com.ayd.parkcontrol.domain.exception.PlateChangeRequestNotFoundException;
import com.ayd.parkcontrol.domain.repository.ChangeRequestEvidenceRepository;
import com.ayd.parkcontrol.domain.repository.PlateChangeRequestRepository;
import com.ayd.parkcontrol.domain.repository.UserRepository;
import com.ayd.parkcontrol.infrastructure.persistence.repository.JpaChangeRequestStatusRepository;
import com.ayd.parkcontrol.infrastructure.persistence.repository.JpaDocumentTypeRepository;
import com.ayd.parkcontrol.infrastructure.persistence.repository.JpaPlateChangeReasonRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class GetPlateChangeRequestUseCase {

    private final PlateChangeRequestRepository plateChangeRequestRepository;
    private final UserRepository userRepository;
    private final ChangeRequestEvidenceRepository evidenceRepository;
    private final JpaPlateChangeReasonRepository reasonRepository;
    private final JpaChangeRequestStatusRepository statusRepository;
    private final JpaDocumentTypeRepository documentTypeRepository;
    private final PlateChangeRequestDtoMapper mapper;

    @Transactional(readOnly = true)
    public PlateChangeRequestResponse execute(Long id) {
        log.info("Getting plate change request with ID: {}", id);

        var request = plateChangeRequestRepository.findById(id)
                .orElseThrow(() -> new PlateChangeRequestNotFoundException(id));

        String userName = userRepository.findById(request.getUserId())
                .map(u -> u.getFirstName() + " " + u.getLastName())
                .orElse("Unknown");

        String reasonName = reasonRepository.findById(request.getReasonId())
                .map(r -> r.getName())
                .orElse("Unknown");

        var status = statusRepository.findById(request.getStatusId()).orElse(null);
        String statusCode = status != null ? status.getCode() : "UNKNOWN";
        String statusName = status != null ? status.getName() : "Unknown";

        String reviewerName = request.getReviewedBy() != null ? userRepository.findById(request.getReviewedBy())
                .map(u -> u.getFirstName() + " " + u.getLastName())
                .orElse(null) : null;

        // Obtener todas las evidencias
        var evidences = evidenceRepository.findByChangeRequestId(request.getId());
        
        List<EvidenceResponse> evidenceResponses = evidences.stream()
                .map(evidence -> {
                    var documentType = documentTypeRepository.findById(evidence.getDocumentTypeId()).orElse(null);
                    String documentTypeCode = documentType != null ? documentType.getCode() : "UNKNOWN";
                    String documentTypeName = documentType != null ? documentType.getName() : "Unknown";
                    
                    return mapper.toEvidenceResponse(evidence, documentTypeCode, documentTypeName);
                })
                .collect(Collectors.toList());

        log.info("Found {} evidences for plate change request {}", evidenceResponses.size(), id);

        return mapper.toResponse(request, userName, reasonName, statusCode, statusName, reviewerName, evidenceResponses);
    }
}
