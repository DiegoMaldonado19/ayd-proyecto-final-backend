package com.ayd.parkcontrol.application.usecase.validation;

import com.ayd.parkcontrol.application.dto.response.validation.PlateChangeRequestResponse;
import com.ayd.parkcontrol.application.mapper.PlateChangeRequestDtoMapper;
import com.ayd.parkcontrol.domain.repository.ChangeRequestEvidenceRepository;
import com.ayd.parkcontrol.domain.repository.PlateChangeRequestRepository;
import com.ayd.parkcontrol.domain.repository.UserRepository;
import com.ayd.parkcontrol.infrastructure.persistence.repository.JpaChangeRequestStatusRepository;
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
public class ListPlateChangeRequestsByUserUseCase {

    private final PlateChangeRequestRepository plateChangeRequestRepository;
    private final UserRepository userRepository;
    private final ChangeRequestEvidenceRepository evidenceRepository;
    private final JpaPlateChangeReasonRepository reasonRepository;
    private final JpaChangeRequestStatusRepository statusRepository;
    private final PlateChangeRequestDtoMapper mapper;

    @Transactional(readOnly = true)
    public List<PlateChangeRequestResponse> execute(Long userId) {
        log.info("Listing plate change requests for user: {}", userId);

        return plateChangeRequestRepository.findByUserId(userId).stream()
                .map(request -> {
                    String userName = userRepository.findById(request.getUserId())
                            .map(u -> u.getFirstName() + " " + u.getLastName())
                            .orElse("Unknown");

                    String reasonName = reasonRepository.findById(request.getReasonId())
                            .map(r -> r.getName())
                            .orElse("Unknown");

                    var status = statusRepository.findById(request.getStatusId()).orElse(null);
                    String statusCode = status != null ? status.getCode() : "UNKNOWN";
                    String statusName = status != null ? status.getName() : "Unknown";

                    String reviewerName = request.getReviewedBy() != null
                            ? userRepository.findById(request.getReviewedBy())
                                    .map(u -> u.getFirstName() + " " + u.getLastName())
                                    .orElse(null)
                            : null;

                    long evidenceCount = evidenceRepository.countByChangeRequestId(request.getId());

                    return mapper.toResponse(request, userName, reasonName, statusCode, statusName, reviewerName,
                            evidenceCount);
                })
                .collect(Collectors.toList());
    }
}
