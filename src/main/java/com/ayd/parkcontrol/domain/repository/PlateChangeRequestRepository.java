package com.ayd.parkcontrol.domain.repository;

import com.ayd.parkcontrol.domain.model.validation.PlateChangeRequest;

import java.util.List;
import java.util.Optional;

public interface PlateChangeRequestRepository {

    PlateChangeRequest save(PlateChangeRequest plateChangeRequest);

    Optional<PlateChangeRequest> findById(Long id);

    List<PlateChangeRequest> findAll();

    List<PlateChangeRequest> findByStatusId(Integer statusId);

    List<PlateChangeRequest> findByUserId(Long userId);

    List<PlateChangeRequest> findBySubscriptionId(Long subscriptionId);

    List<PlateChangeRequest> findByLicensePlate(String plate);

    boolean hasActivePendingRequest(Long subscriptionId);

    void deleteById(Long id);
}
