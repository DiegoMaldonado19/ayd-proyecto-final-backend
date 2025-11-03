package com.ayd.parkcontrol.application.usecase.commerce;

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
import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GetCommerceByIdUseCaseTest {

    @Mock
    private JpaAffiliatedBusinessRepository commerceRepository;

    @InjectMocks
    private GetCommerceByIdUseCase getCommerceByIdUseCase;

    private AffiliatedBusinessEntity commerce;

    @BeforeEach
    void setUp() {
        commerce = AffiliatedBusinessEntity.builder()
                .id(1L)
                .name("Test Commerce")
                .taxId("12345678")
                .contactName("John Doe")
                .email("test@commerce.com")
                .phone("12345678")
                .ratePerHour(BigDecimal.valueOf(10.00))
                .isActive(true)
                .createdAt(LocalDateTime.of(2025, 1, 1, 0, 0))
                .updatedAt(LocalDateTime.of(2025, 1, 1, 0, 0))
                .build();
    }

    @Test
    void execute_shouldReturnCommerce_whenCommerceExists() {
        when(commerceRepository.findById(anyLong())).thenReturn(Optional.of(commerce));

        CommerceResponse result = getCommerceByIdUseCase.execute(1L);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getName()).isEqualTo("Test Commerce");
        assertThat(result.getTaxId()).isEqualTo("12345678");
        assertThat(result.getContactName()).isEqualTo("John Doe");
        assertThat(result.getEmail()).isEqualTo("test@commerce.com");
        assertThat(result.getPhone()).isEqualTo("12345678");
        assertThat(result.getRatePerHour()).isEqualByComparingTo(BigDecimal.valueOf(10.00));
        assertThat(result.getIsActive()).isTrue();
    }

    @Test
    void execute_shouldThrowNotFoundException_whenCommerceDoesNotExist() {
        when(commerceRepository.findById(anyLong())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> getCommerceByIdUseCase.execute(999L))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("Commerce not found with ID: 999");
    }
}
