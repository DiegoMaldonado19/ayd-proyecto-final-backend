package com.ayd.parkcontrol.application.usecase.commerce;

import com.ayd.parkcontrol.application.dto.request.commerce.UpdateCommerceRequest;
import com.ayd.parkcontrol.application.dto.response.commerce.CommerceResponse;
import com.ayd.parkcontrol.domain.exception.NotFoundException;
import com.ayd.parkcontrol.infrastructure.persistence.entity.AffiliatedBusinessEntity;
import com.ayd.parkcontrol.infrastructure.persistence.repository.JpaAffiliatedBusinessRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UpdateCommerceUseCaseTest {

    @Mock
    private JpaAffiliatedBusinessRepository commerceRepository;

    @InjectMocks
    private UpdateCommerceUseCase updateCommerceUseCase;

    private AffiliatedBusinessEntity commerce;

    @BeforeEach
    void setUp() {
        commerce = AffiliatedBusinessEntity.builder()
                .id(1L)
                .name("Old Commerce")
                .taxId("12345678")
                .contactName("Old Contact")
                .email("old@commerce.com")
                .phone("11111111")
                .ratePerHour(BigDecimal.valueOf(10.00))
                .isActive(true)
                .build();
    }

    @Test
    void execute_shouldUpdateAllFields_whenAllFieldsProvided() {
        UpdateCommerceRequest request = UpdateCommerceRequest.builder()
                .name("New Commerce")
                .contactName("New Contact")
                .email("new@commerce.com")
                .phone("22222222")
                .ratePerHour(BigDecimal.valueOf(15.00))
                .isActive(false)
                .build();

        when(commerceRepository.findById(anyLong())).thenReturn(Optional.of(commerce));
        when(commerceRepository.save(any(AffiliatedBusinessEntity.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        CommerceResponse result = updateCommerceUseCase.execute(1L, request);

        assertThat(result).isNotNull();
        assertThat(result.getName()).isEqualTo("New Commerce");
        assertThat(result.getContactName()).isEqualTo("New Contact");
        assertThat(result.getEmail()).isEqualTo("new@commerce.com");
        assertThat(result.getPhone()).isEqualTo("22222222");
        assertThat(result.getRatePerHour()).isEqualByComparingTo(BigDecimal.valueOf(15.00));
        assertThat(result.getIsActive()).isFalse();
        verify(commerceRepository).save(commerce);
    }

    @Test
    void execute_shouldUpdateOnlyProvidedFields_whenPartialUpdateRequested() {
        UpdateCommerceRequest request = UpdateCommerceRequest.builder()
                .name("Updated Name")
                .ratePerHour(BigDecimal.valueOf(12.00))
                .build();

        when(commerceRepository.findById(anyLong())).thenReturn(Optional.of(commerce));
        when(commerceRepository.save(any(AffiliatedBusinessEntity.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        CommerceResponse result = updateCommerceUseCase.execute(1L, request);

        assertThat(result).isNotNull();
        assertThat(result.getName()).isEqualTo("Updated Name");
        assertThat(result.getRatePerHour()).isEqualByComparingTo(BigDecimal.valueOf(12.00));
        assertThat(result.getContactName()).isEqualTo("Old Contact");
        assertThat(result.getEmail()).isEqualTo("old@commerce.com");
        assertThat(result.getPhone()).isEqualTo("11111111");
        assertThat(result.getIsActive()).isTrue();
    }

    @Test
    void execute_shouldThrowNotFoundException_whenCommerceDoesNotExist() {
        UpdateCommerceRequest request = UpdateCommerceRequest.builder()
                .name("New Name")
                .build();

        when(commerceRepository.findById(anyLong())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> updateCommerceUseCase.execute(999L, request))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("Commerce not found with ID: 999");

        verify(commerceRepository, never()).save(any());
    }
}
