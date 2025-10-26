package com.ayd.parkcontrol.application.usecase.commerce;

import com.ayd.parkcontrol.application.dto.request.commerce.UpdateCommerceRequest;
import com.ayd.parkcontrol.application.dto.response.commerce.CommerceResponse;
import com.ayd.parkcontrol.domain.exception.NotFoundException;
import com.ayd.parkcontrol.infrastructure.persistence.entity.AffiliatedBusinessEntity;
import com.ayd.parkcontrol.infrastructure.persistence.repository.JpaAffiliatedBusinessRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class UpdateCommerceUseCase {

    private final JpaAffiliatedBusinessRepository commerceRepository;

    @Transactional
    public CommerceResponse execute(Long id, UpdateCommerceRequest request) {
        log.info("Updating commerce with ID: {}", id);

        AffiliatedBusinessEntity commerce = commerceRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Commerce not found with ID: " + id));

        if (request.getName() != null) {
            commerce.setName(request.getName());
        }
        if (request.getContactName() != null) {
            commerce.setContactName(request.getContactName());
        }
        if (request.getEmail() != null) {
            commerce.setEmail(request.getEmail());
        }
        if (request.getPhone() != null) {
            commerce.setPhone(request.getPhone());
        }
        if (request.getRatePerHour() != null) {
            commerce.setRatePerHour(request.getRatePerHour());
        }
        if (request.getIsActive() != null) {
            commerce.setIsActive(request.getIsActive());
        }

        AffiliatedBusinessEntity updated = commerceRepository.save(commerce);

        log.info("Commerce updated successfully: {}", updated.getName());

        return mapToResponse(updated);
    }

    private CommerceResponse mapToResponse(AffiliatedBusinessEntity entity) {
        return CommerceResponse.builder()
                .id(entity.getId())
                .name(entity.getName())
                .taxId(entity.getTaxId())
                .contactName(entity.getContactName())
                .email(entity.getEmail())
                .phone(entity.getPhone())
                .ratePerHour(entity.getRatePerHour())
                .isActive(entity.getIsActive())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }
}
