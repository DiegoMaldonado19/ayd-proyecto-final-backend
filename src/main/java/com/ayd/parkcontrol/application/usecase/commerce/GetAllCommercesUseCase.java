package com.ayd.parkcontrol.application.usecase.commerce;

import com.ayd.parkcontrol.application.dto.response.commerce.CommerceResponse;
import com.ayd.parkcontrol.infrastructure.persistence.entity.AffiliatedBusinessEntity;
import com.ayd.parkcontrol.infrastructure.persistence.repository.JpaAffiliatedBusinessRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class GetAllCommercesUseCase {

    private final JpaAffiliatedBusinessRepository commerceRepository;

    @Transactional(readOnly = true)
    public Page<CommerceResponse> execute(Pageable pageable) {
        log.info("Fetching all commerces - Page: {}, Size: {}", pageable.getPageNumber(), pageable.getPageSize());

        Page<AffiliatedBusinessEntity> commerces = commerceRepository.findAll(pageable);

        log.info("Found {} commerces", commerces.getTotalElements());

        return commerces.map(this::mapToResponse);
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
