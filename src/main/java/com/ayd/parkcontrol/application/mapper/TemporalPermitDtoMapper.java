package com.ayd.parkcontrol.application.mapper;

import com.ayd.parkcontrol.application.dto.response.validation.TemporalPermitResponse;
import com.ayd.parkcontrol.infrastructure.persistence.entity.TemporalPermitEntity;
import com.ayd.parkcontrol.infrastructure.persistence.entity.UserEntity;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class TemporalPermitDtoMapper {

    private final ObjectMapper objectMapper;

    public TemporalPermitDtoMapper(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public TemporalPermitResponse toResponse(TemporalPermitEntity entity, UserEntity approver) {
        List<Long> allowedBranches = parseAllowedBranches(entity.getAllowedBranches());

        return TemporalPermitResponse.builder()
                .id(entity.getId())
                .subscriptionId(entity.getSubscriptionId())
                .temporalPlate(entity.getTemporalPlate())
                .startDate(entity.getStartDate())
                .endDate(entity.getEndDate())
                .maxUses(entity.getMaxUses())
                .currentUses(entity.getCurrentUses())
                .allowedBranches(allowedBranches)
                .vehicleType(entity.getVehicleType() != null ? entity.getVehicleType().getName() : null)
                .status(entity.getStatus() != null ? entity.getStatus().getName() : null)
                .approvedBy(approver != null ? approver.getFirstName() + " " + approver.getLastName() : null)
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }

    public String serializeAllowedBranches(List<Long> allowedBranches) {
        if (allowedBranches == null || allowedBranches.isEmpty()) {
            return null;
        }
        try {
            return objectMapper.writeValueAsString(allowedBranches);
        } catch (JsonProcessingException e) {
            return null;
        }
    }

    private List<Long> parseAllowedBranches(String json) {
        if (json == null || json.isBlank()) {
            return new ArrayList<>();
        }
        try {
            return objectMapper.readValue(json, new TypeReference<List<Long>>() {
            });
        } catch (JsonProcessingException e) {
            return new ArrayList<>();
        }
    }
}
