package com.ayd.parkcontrol.application.usecase.validation;

import com.ayd.parkcontrol.application.dto.request.validation.CreatePlateChangeRequest;
import com.ayd.parkcontrol.application.dto.response.validation.PlateChangeRequestResponse;
import com.ayd.parkcontrol.application.mapper.PlateChangeRequestDtoMapper;
import com.ayd.parkcontrol.domain.exception.PendingPlateChangeRequestException;
import com.ayd.parkcontrol.domain.exception.SubscriptionNotFoundException;
import com.ayd.parkcontrol.domain.exception.UserNotFoundException;
import com.ayd.parkcontrol.domain.model.validation.PlateChangeRequest;
import com.ayd.parkcontrol.domain.repository.ChangeRequestEvidenceRepository;
import com.ayd.parkcontrol.domain.repository.PlateChangeRequestRepository;
import com.ayd.parkcontrol.domain.repository.SubscriptionRepository;
import com.ayd.parkcontrol.domain.repository.UserRepository;
import com.ayd.parkcontrol.infrastructure.persistence.repository.JpaChangeRequestStatusRepository;
import com.ayd.parkcontrol.infrastructure.persistence.repository.JpaPlateChangeReasonRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class CreatePlateChangeRequestUseCase {

    private final PlateChangeRequestRepository plateChangeRequestRepository;
    private final SubscriptionRepository subscriptionRepository;
    private final UserRepository userRepository;
    private final ChangeRequestEvidenceRepository evidenceRepository;
    private final JpaPlateChangeReasonRepository reasonRepository;
    private final JpaChangeRequestStatusRepository statusRepository;
    private final PlateChangeRequestDtoMapper mapper;

    @Transactional
    public PlateChangeRequestResponse execute(CreatePlateChangeRequest request) {
        log.info("Creating plate change request for subscription: {}", request.getSubscription_id());

        subscriptionRepository.findById(request.getSubscription_id())
                .orElseThrow(() -> new SubscriptionNotFoundException(request.getSubscription_id()));

        var user = userRepository.findById(request.getUser_id())
                .orElseThrow(() -> new UserNotFoundException(request.getUser_id()));

        if (plateChangeRequestRepository.hasActivePendingRequest(request.getSubscription_id())) {
            throw new PendingPlateChangeRequestException(request.getSubscription_id());
        }

        PlateChangeRequest plateChangeRequest = mapper.toDomain(request);
        PlateChangeRequest saved = plateChangeRequestRepository.save(plateChangeRequest);

        String userName = user.getFirstName() + " " + user.getLastName();
        String reasonName = reasonRepository.findById(saved.getReasonId())
                .map(r -> r.getName())
                .orElse("Unknown");
        var status = statusRepository.findById(saved.getStatusId()).orElse(null);
        String statusCode = status != null ? status.getCode() : "PENDING";
        String statusName = status != null ? status.getName() : "Pendiente";

        long evidenceCount = evidenceRepository.countByChangeRequestId(saved.getId());

        log.info("Plate change request created with ID: {}", saved.getId());

        return mapper.toResponse(saved, userName, reasonName, statusCode, statusName, null, evidenceCount);
    }
}
