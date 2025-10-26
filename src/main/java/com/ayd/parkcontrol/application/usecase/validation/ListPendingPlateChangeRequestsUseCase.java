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
public class ListPendingPlateChangeRequestsUseCase {

    private final PlateChangeRequestRepository plateChangeRequestRepository;
    private final UserRepository userRepository;
    private final ChangeRequestEvidenceRepository evidenceRepository;
    private final JpaPlateChangeReasonRepository reasonRepository;
    private final JpaChangeRequestStatusRepository statusRepository;
    private final PlateChangeRequestDtoMapper mapper;

    @Transactional(readOnly = true)
    public List<PlateChangeRequestResponse> execute() {
        log.info("Listing pending plate change requests");

        var pendingStatus = statusRepository.findByCode("PENDING").orElse(null);
        if (pendingStatus == null) {
            log.warn("PENDING status not found");
            return List.of();
        }

        return plateChangeRequestRepository.findByStatusId(pendingStatus.getId()).stream()
                .map(request -> {
                    String userName = userRepository.findById(request.getUserId())
                            .map(u -> u.getFirstName() + " " + u.getLastName())
                            .orElse("Unknown");

                    String reasonName = reasonRepository.findById(request.getReasonId())
                            .map(r -> r.getName())
                            .orElse("Unknown");

                    long evidenceCount = evidenceRepository.countByChangeRequestId(request.getId());

                    return mapper.toResponse(request, userName, reasonName, pendingStatus.getCode(),
                            pendingStatus.getName(), null, evidenceCount);
                })
                .collect(Collectors.toList());
    }
}
