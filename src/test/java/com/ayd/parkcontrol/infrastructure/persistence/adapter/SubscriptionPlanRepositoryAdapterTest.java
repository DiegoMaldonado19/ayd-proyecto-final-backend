package com.ayd.parkcontrol.infrastructure.persistence.adapter;

import com.ayd.parkcontrol.domain.model.subscription.SubscriptionPlan;
import com.ayd.parkcontrol.infrastructure.persistence.entity.SubscriptionPlanEntity;
import com.ayd.parkcontrol.infrastructure.persistence.mapper.SubscriptionMapper;
import com.ayd.parkcontrol.infrastructure.persistence.repository.JpaSubscriptionPlanRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SubscriptionPlanRepositoryAdapterTest {

    @Mock
    private JpaSubscriptionPlanRepository jpaRepository;

    @Mock
    private SubscriptionMapper mapper;

    @InjectMocks
    private SubscriptionPlanRepositoryAdapter adapter;

    private SubscriptionPlan plan;
    private SubscriptionPlanEntity entity;

    @BeforeEach
    void setUp() {
        plan = new SubscriptionPlan();
        plan.setId(1L);

        entity = new SubscriptionPlanEntity();
        entity.setId(1L);
    }

    @Test
    void save_ShouldMapToEntityCallRepositoryAndMapToDomain() {
        // Arrange
        when(mapper.toEntity(plan)).thenReturn(entity);
        when(jpaRepository.save(entity)).thenReturn(entity);
        when(mapper.toDomain(entity)).thenReturn(plan);

        // Act
        SubscriptionPlan result = adapter.save(plan);

        // Assert
        assertThat(result).isNotNull();
        verify(mapper).toEntity(plan);
        verify(jpaRepository).save(entity);
        verify(mapper).toDomain(entity);
    }

    @Test
    void findById_WhenPlanExists_ShouldReturnMappedPlan() {
        // Arrange
        when(jpaRepository.findByIdWithPlanType(1L)).thenReturn(Optional.of(entity));
        when(mapper.toDomain(entity)).thenReturn(plan);

        // Act
        Optional<SubscriptionPlan> result = adapter.findById(1L);

        // Assert
        assertThat(result).isPresent();
        verify(jpaRepository).findByIdWithPlanType(1L);
        verify(mapper).toDomain(entity);
    }

    @Test
    void findById_WhenPlanDoesNotExist_ShouldReturnEmpty() {
        // Arrange
        when(jpaRepository.findByIdWithPlanType(999L)).thenReturn(Optional.empty());

        // Act
        Optional<SubscriptionPlan> result = adapter.findById(999L);

        // Assert
        assertThat(result).isEmpty();
        verify(jpaRepository).findByIdWithPlanType(999L);
        verify(mapper, times(0)).toDomain(entity);
    }

    @Test
    void findAllActive_ShouldReturnActiveSubscriptionPlans() {
        // Arrange
        List<SubscriptionPlanEntity> entities = Collections.singletonList(entity);
        when(jpaRepository.findByIsActive(true)).thenReturn(entities);
        when(mapper.toDomain(entity)).thenReturn(plan);

        // Act
        List<SubscriptionPlan> result = adapter.findAllActive();

        // Assert
        assertThat(result).hasSize(1);
        verify(jpaRepository).findByIsActive(true);
    }
}
