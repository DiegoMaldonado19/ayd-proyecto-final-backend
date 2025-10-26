package com.ayd.parkcontrol.application.usecase.commerce;

import com.ayd.parkcontrol.application.dto.request.commerce.CreateCommerceRequest;
import com.ayd.parkcontrol.application.dto.response.commerce.CommerceResponse;
import com.ayd.parkcontrol.domain.exception.BusinessRuleException;
import com.ayd.parkcontrol.infrastructure.persistence.entity.AffiliatedBusinessEntity;
import com.ayd.parkcontrol.infrastructure.persistence.repository.JpaAffiliatedBusinessRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class CreateCommerceUseCase {

    private final JpaAffiliatedBusinessRepository commerceRepository;

    @Transactional
    public CommerceResponse execute(CreateCommerceRequest request) {
        log.info("Creating commerce with tax ID: {}", request.getTaxId());

        commerceRepository.findByTaxId(request.getTaxId()).ifPresent(commerce -> {
            throw new BusinessRuleException("Commerce with tax ID " + request.getTaxId() + " already exists");
        });

        AffiliatedBusinessEntity commerce = AffiliatedBusinessEntity.builder()
                .name(request.getName())
                .taxId(request.getTaxId())
                .contactName(request.getContactName())
                .email(request.getEmail())
                .phone(request.getPhone())
                .ratePerHour(request.getRatePerHour())
                .isActive(true)
                .build();

        AffiliatedBusinessEntity saved = commerceRepository.save(commerce);

        log.info("Commerce created successfully with ID: {}", saved.getId());

        return mapToResponse(saved);
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
