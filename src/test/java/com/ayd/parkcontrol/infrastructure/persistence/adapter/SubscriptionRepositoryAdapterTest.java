package com.ayd.parkcontrol.infrastructure.persistence.adapter;

import com.ayd.parkcontrol.domain.model.subscription.Subscription;
import com.ayd.parkcontrol.infrastructure.persistence.entity.SubscriptionEntity;
import com.ayd.parkcontrol.infrastructure.persistence.mapper.SubscriptionMapper;
import com.ayd.parkcontrol.infrastructure.persistence.repository.JpaSubscriptionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SubscriptionRepositoryAdapterTest {

    @Mock
    private JpaSubscriptionRepository jpaRepository;

    @Mock
    private SubscriptionMapper mapper;

    @InjectMocks
    private SubscriptionRepositoryAdapter adapter;

    private Subscription subscription;
    private SubscriptionEntity entity;

    @BeforeEach
    void setUp() {
        subscription = new Subscription();
        subscription.setId(1L);

        entity = new SubscriptionEntity();
        entity.setId(1L);
    }

    @Test
    void save_ShouldMapToEntityCallRepositoryAndMapToDomain() {
        // Arrange
        when(mapper.toEntity(subscription)).thenReturn(entity);
        when(jpaRepository.save(entity)).thenReturn(entity);
        when(jpaRepository.findByIdWithDetails(1L)).thenReturn(Optional.of(entity));
        when(mapper.toDomain(entity)).thenReturn(subscription);

        // Act
        Subscription result = adapter.save(subscription);

        // Assert
        assertThat(result).isNotNull();
        verify(mapper).toEntity(subscription);
        verify(jpaRepository).save(entity);
    }

    @Test
    void findById_WhenSubscriptionExists_ShouldReturnMappedSubscription() {
        // Arrange
        when(jpaRepository.findById(1L)).thenReturn(Optional.of(entity));
        when(mapper.toDomain(entity)).thenReturn(subscription);

        // Act
        Optional<Subscription> result = adapter.findById(1L);

        // Assert
        assertThat(result).isPresent();
        verify(jpaRepository).findById(1L);
        verify(mapper).toDomain(entity);
    }

    @Test
    void findById_WhenSubscriptionDoesNotExist_ShouldReturnEmpty() {
        // Arrange
        when(jpaRepository.findById(999L)).thenReturn(Optional.empty());

        // Act
        Optional<Subscription> result = adapter.findById(999L);

        // Assert
        assertThat(result).isEmpty();
        verify(jpaRepository).findById(999L);
        verify(mapper, times(0)).toDomain(entity);
    }

    @Test
    void findByIdWithDetails_ShouldReturnSubscriptionWithDetails() {
        // Arrange
        when(jpaRepository.findByIdWithDetails(1L)).thenReturn(Optional.of(entity));
        when(mapper.toDomain(entity)).thenReturn(subscription);

        // Act
        Optional<Subscription> result = adapter.findByIdWithDetails(1L);

        // Assert
        assertThat(result).isPresent();
        verify(jpaRepository).findByIdWithDetails(1L);
    }
}
