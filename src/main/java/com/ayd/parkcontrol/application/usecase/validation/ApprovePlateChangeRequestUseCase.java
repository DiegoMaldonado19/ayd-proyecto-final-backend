package com.ayd.parkcontrol.application.usecase.validation;

import com.ayd.parkcontrol.application.dto.request.validation.ApprovePlateChangeRequest;
import com.ayd.parkcontrol.application.dto.response.validation.PlateChangeRequestResponse;
import com.ayd.parkcontrol.application.mapper.PlateChangeRequestDtoMapper;
import com.ayd.parkcontrol.domain.exception.InvalidPlateChangeStatusException;
import com.ayd.parkcontrol.domain.exception.PlateChangeRequestNotFoundException;
import com.ayd.parkcontrol.domain.repository.ChangeRequestEvidenceRepository;
import com.ayd.parkcontrol.domain.repository.PlateChangeRequestRepository;
import com.ayd.parkcontrol.domain.repository.SubscriptionRepository;
import com.ayd.parkcontrol.domain.repository.UserRepository;
import com.ayd.parkcontrol.infrastructure.persistence.repository.JpaChangeRequestStatusRepository;
import com.ayd.parkcontrol.infrastructure.persistence.repository.JpaPlateChangeReasonRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
public class ApprovePlateChangeRequestUseCase {

    private final PlateChangeRequestRepository plateChangeRequestRepository;
    private final SubscriptionRepository subscriptionRepository;
    private final UserRepository userRepository;
    private final ChangeRequestEvidenceRepository evidenceRepository;
    private final JpaPlateChangeReasonRepository reasonRepository;
    private final JpaChangeRequestStatusRepository statusRepository;
    private final PlateChangeRequestDtoMapper mapper;

    @Transactional
    public PlateChangeRequestResponse execute(Long id, ApprovePlateChangeRequest request) {
        log.info("Approving plate change request with ID: {}", id);

        var plateChangeRequest = plateChangeRequestRepository.findById(id)
                .orElseThrow(() -> new PlateChangeRequestNotFoundException(id));

        var status = statusRepository.findById(plateChangeRequest.getStatusId()).orElse(null);
        if (status == null || !"PENDING".equals(status.getCode())) {
            throw new InvalidPlateChangeStatusException("Solo se pueden aprobar solicitudes con estado PENDIENTE");
        }

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String reviewerEmail = authentication.getName();
        var reviewer = userRepository.findByEmail(reviewerEmail).orElse(null);

        var approvedStatus = statusRepository.findByCode("APPROVED").orElse(null);
        if (approvedStatus == null) {
            throw new InvalidPlateChangeStatusException("Estado APPROVED no encontrado");
        }

        plateChangeRequest.setStatusId(approvedStatus.getId());
        plateChangeRequest.setReviewedBy(reviewer != null ? reviewer.getId() : null);
        plateChangeRequest.setReviewedAt(LocalDateTime.now());
        plateChangeRequest.setReviewNotes(request.getReview_notes());

        var savedRequest = plateChangeRequestRepository.save(plateChangeRequest);

        var subscription = subscriptionRepository.findById(savedRequest.getSubscriptionId()).orElse(null);
        if (subscription != null) {
            subscription.setLicensePlate(savedRequest.getNewLicensePlate());
            subscriptionRepository.save(subscription);
            log.info("Updated subscription license plate to: {}", savedRequest.getNewLicensePlate());
        }

        String userName = userRepository.findById(savedRequest.getUserId())
                .map(u -> u.getFirstName() + " " + u.getLastName())
                .orElse("Unknown");

        String reasonName = reasonRepository.findById(savedRequest.getReasonId())
                .map(r -> r.getName())
                .orElse("Unknown");

        String reviewerName = reviewer != null ? reviewer.getFirstName() + " " + reviewer.getLastName() : null;
        long evidenceCount = evidenceRepository.countByChangeRequestId(savedRequest.getId());

        log.info("Plate change request approved successfully");

        return mapper.toResponse(savedRequest, userName, reasonName, approvedStatus.getCode(),
                approvedStatus.getName(), reviewerName, evidenceCount);
    }
}
