package com.ayd.parkcontrol.infrastructure.persistence.adapter;

import com.ayd.parkcontrol.domain.model.validation.PlateChangeRequest;
import com.ayd.parkcontrol.domain.repository.PlateChangeRequestRepository;
import com.ayd.parkcontrol.infrastructure.persistence.entity.PlateChangeRequestEntity;
import com.ayd.parkcontrol.infrastructure.persistence.mapper.PlateChangeRequestMapper;
import com.ayd.parkcontrol.infrastructure.persistence.repository.JpaPlateChangeRequestRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class PlateChangeRequestRepositoryAdapter implements PlateChangeRequestRepository {

    private final JpaPlateChangeRequestRepository jpaRepository;
    private final PlateChangeRequestMapper mapper;

    @Override
    public PlateChangeRequest save(PlateChangeRequest plateChangeRequest) {
        PlateChangeRequestEntity entity = mapper.toEntity(plateChangeRequest);
        PlateChangeRequestEntity savedEntity = jpaRepository.save(entity);
        return mapper.toDomain(savedEntity);
    }

    @Override
    public Optional<PlateChangeRequest> findById(Long id) {
        return jpaRepository.findById(id)
                .map(mapper::toDomain);
    }

    @Override
    public List<PlateChangeRequest> findAll() {
        return jpaRepository.findAllOrderByCreatedAtDesc().stream()
                .map(mapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public List<PlateChangeRequest> findByStatusId(Integer statusId) {
        return jpaRepository.findByStatusId(statusId).stream()
                .map(mapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public List<PlateChangeRequest> findByUserId(Long userId) {
        return jpaRepository.findByUserId(userId).stream()
                .map(mapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public List<PlateChangeRequest> findBySubscriptionId(Long subscriptionId) {
        return jpaRepository.findBySubscriptionId(subscriptionId).stream()
                .map(mapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public List<PlateChangeRequest> findByLicensePlate(String plate) {
        return jpaRepository.findByLicensePlate(plate).stream()
                .map(mapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public boolean hasActivePendingRequest(Long subscriptionId) {
        return jpaRepository.hasActivePendingRequest(subscriptionId);
    }

    @Override
    public void deleteById(Long id) {
        jpaRepository.deleteById(id);
    }
}
