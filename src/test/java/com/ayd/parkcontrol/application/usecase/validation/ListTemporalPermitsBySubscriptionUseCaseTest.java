package com.ayd.parkcontrol.application.usecase.validation;

import com.ayd.parkcontrol.application.dto.response.validation.TemporalPermitResponse;
import com.ayd.parkcontrol.application.mapper.TemporalPermitDtoMapper;
import com.ayd.parkcontrol.domain.exception.SubscriptionNotFoundException;
import com.ayd.parkcontrol.infrastructure.persistence.entity.SubscriptionEntity;
import com.ayd.parkcontrol.infrastructure.persistence.entity.TemporalPermitEntity;
import com.ayd.parkcontrol.infrastructure.persistence.entity.UserEntity;
import com.ayd.parkcontrol.infrastructure.persistence.repository.JpaSubscriptionRepository;
import com.ayd.parkcontrol.infrastructure.persistence.repository.JpaTemporalPermitRepository;
import com.ayd.parkcontrol.infrastructure.persistence.repository.JpaUserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ListTemporalPermitsBySubscriptionUseCaseTest {

    @Mock
    private JpaTemporalPermitRepository temporalPermitRepository;

    @Mock
    private JpaSubscriptionRepository subscriptionRepository;

    @Mock
    private JpaUserRepository userRepository;

    @Mock
    private TemporalPermitDtoMapper mapper;

    @InjectMocks
    private ListTemporalPermitsBySubscriptionUseCase useCase;

    @Test
    void execute_ShouldReturnPermitsForSubscription() {
        SubscriptionEntity subscription = SubscriptionEntity.builder().id(100L).build();

        TemporalPermitEntity permit1 = TemporalPermitEntity.builder()
                .id(1L)
                .subscriptionId(100L)
                .approvedBy(5L)
                .build();

        TemporalPermitEntity permit2 = TemporalPermitEntity.builder()
                .id(2L)
                .subscriptionId(100L)
                .approvedBy(6L)
                .build();

        UserEntity approver1 = UserEntity.builder().id(5L).build();
        UserEntity approver2 = UserEntity.builder().id(6L).build();

        TemporalPermitResponse response1 = TemporalPermitResponse.builder().id(1L).build();
        TemporalPermitResponse response2 = TemporalPermitResponse.builder().id(2L).build();

        when(subscriptionRepository.findById(100L)).thenReturn(Optional.of(subscription));
        when(temporalPermitRepository.findBySubscriptionIdOrderByCreatedAtDesc(100L))
                .thenReturn(Arrays.asList(permit1, permit2));
        when(userRepository.findById(5L)).thenReturn(Optional.of(approver1));
        when(userRepository.findById(6L)).thenReturn(Optional.of(approver2));
        when(mapper.toResponse(permit1, approver1)).thenReturn(response1);
        when(mapper.toResponse(permit2, approver2)).thenReturn(response2);

        List<TemporalPermitResponse> result = useCase.execute(100L);

        assertThat(result).hasSize(2);
    }

    @Test
    void execute_ShouldThrowException_WhenSubscriptionNotFound() {
        when(subscriptionRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> useCase.execute(999L))
                .isInstanceOf(SubscriptionNotFoundException.class);
    }

    @Test
    void execute_ShouldReturnEmptyList_WhenNoPermitsForSubscription() {
        SubscriptionEntity subscription = SubscriptionEntity.builder().id(100L).build();

        when(subscriptionRepository.findById(100L)).thenReturn(Optional.of(subscription));
        when(temporalPermitRepository.findBySubscriptionIdOrderByCreatedAtDesc(100L))
                .thenReturn(Arrays.asList());

        List<TemporalPermitResponse> result = useCase.execute(100L);

        assertThat(result).isEmpty();
    }
}
