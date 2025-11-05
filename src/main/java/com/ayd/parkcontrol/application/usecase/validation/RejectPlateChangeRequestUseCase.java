package com.ayd.parkcontrol.application.usecase.validation;

import com.ayd.parkcontrol.application.dto.request.validation.RejectPlateChangeRequest;
import com.ayd.parkcontrol.application.dto.response.validation.PlateChangeRequestResponse;
import com.ayd.parkcontrol.application.mapper.PlateChangeRequestDtoMapper;
import com.ayd.parkcontrol.application.port.notification.EmailService;
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
public class RejectPlateChangeRequestUseCase {

    private final PlateChangeRequestRepository plateChangeRequestRepository;
    private final UserRepository userRepository;
    private final ChangeRequestEvidenceRepository evidenceRepository;
    private final JpaPlateChangeReasonRepository reasonRepository;
    private final JpaChangeRequestStatusRepository statusRepository;
    private final PlateChangeRequestDtoMapper mapper;
    private final EmailService emailService;
    private final SubscriptionRepository subscriptionRepository;

    @Transactional
    public PlateChangeRequestResponse execute(Long id, RejectPlateChangeRequest request) {
        log.info("Rejecting plate change request with ID: {}", id);

        var plateChangeRequest = plateChangeRequestRepository.findById(id)
                .orElseThrow(() -> new PlateChangeRequestNotFoundException(id));

        var status = statusRepository.findById(plateChangeRequest.getStatusId()).orElse(null);
        if (status == null || !"PENDING".equals(status.getCode())) {
            throw new InvalidPlateChangeStatusException("Solo se pueden rechazar solicitudes con estado PENDIENTE");
        }

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String reviewerEmail = authentication.getName();
        var reviewer = userRepository.findByEmail(reviewerEmail).orElse(null);

        var rejectedStatus = statusRepository.findByCode("REJECTED").orElse(null);
        if (rejectedStatus == null) {
            throw new InvalidPlateChangeStatusException("Estado REJECTED no encontrado");
        }

        plateChangeRequest.setStatusId(rejectedStatus.getId());
        plateChangeRequest.setReviewedBy(reviewer != null ? reviewer.getId() : null);
        plateChangeRequest.setReviewedAt(LocalDateTime.now());
        plateChangeRequest.setReviewNotes(request.getReview_notes());

        var savedRequest = plateChangeRequestRepository.save(plateChangeRequest);

        var user = userRepository.findById(savedRequest.getUserId()).orElse(null);
        String userName = user != null ? user.getFirstName() + " " + user.getLastName() : "Unknown";
        String userEmail = user != null ? user.getEmail() : null;

        String reasonName = reasonRepository.findById(savedRequest.getReasonId())
                .map(r -> r.getName())
                .orElse("Unknown");

        String reviewerName = reviewer != null ? reviewer.getFirstName() + " " + reviewer.getLastName() : null;
        long evidenceCount = evidenceRepository.countByChangeRequestId(savedRequest.getId());

        // Enviar notificación por email al usuario
        if (userEmail != null) {
            try {
                var subscription = subscriptionRepository.findById(savedRequest.getSubscriptionId()).orElse(null);
                String currentPlate = subscription != null ? subscription.getLicensePlate() : 
                    (savedRequest.getOldLicensePlate() != null ? savedRequest.getOldLicensePlate() : "N/A");
                    
                emailService.sendPlateChangeRejectedNotification(
                    userEmail, 
                    userName, 
                    currentPlate,
                    savedRequest.getNewLicensePlate(),
                    savedRequest.getReviewNotes()
                );
                log.info("Rejection notification sent to user: {}", userEmail);
            } catch (Exception e) {
                log.error("Error sending rejection notification to user: {}", userEmail, e);
                // No lanzamos la excepción para no afectar el flujo principal
            }
        }

        log.info("Plate change request rejected successfully");

        return mapper.toResponse(savedRequest, userName, reasonName, rejectedStatus.getCode(),
                rejectedStatus.getName(), reviewerName, evidenceCount);
    }
}
