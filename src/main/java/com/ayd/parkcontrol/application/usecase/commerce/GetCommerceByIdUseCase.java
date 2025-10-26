package com.ayd.parkcontrol.application.usecase.commerce;

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
public class GetCommerceByIdUseCase {

    private final JpaAffiliatedBusinessRepository commerceRepository;

    @Transactional(readOnly = true)
    public CommerceResponse execute(Long id) {
        log.info("Fetching commerce with ID: {}", id);

        AffiliatedBusinessEntity commerce = commerceRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Commerce not found with ID: " + id));

        log.info("Commerce found: {}", commerce.getName());

        return mapToResponse(commerce);
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
