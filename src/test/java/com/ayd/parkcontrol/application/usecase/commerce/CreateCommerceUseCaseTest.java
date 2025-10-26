package com.ayd.parkcontrol.application.usecase.commerce;

import com.ayd.parkcontrol.application.dto.request.commerce.CreateCommerceRequest;
import com.ayd.parkcontrol.application.dto.response.commerce.CommerceResponse;
import com.ayd.parkcontrol.domain.exception.BusinessRuleException;
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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CreateCommerceUseCaseTest {

    @Mock
    private JpaAffiliatedBusinessRepository commerceRepository;

    @InjectMocks
    private CreateCommerceUseCase createCommerceUseCase;

    private CreateCommerceRequest request;
    private AffiliatedBusinessEntity commerce;

    @BeforeEach
    void setUp() {
        request = CreateCommerceRequest.builder()
                .name("Restaurant El Portal")
                .taxId("12345678-9")
                .contactName("Juan Perez")
                .email("contact@elportal.com")
                .phone("50212345678")
                .ratePerHour(BigDecimal.valueOf(8.00))
                .build();

        commerce = AffiliatedBusinessEntity.builder()
                .id(1L)
                .name("Restaurant El Portal")
                .taxId("12345678-9")
                .contactName("Juan Perez")
                .email("contact@elportal.com")
                .phone("50212345678")
                .ratePerHour(BigDecimal.valueOf(8.00))
                .isActive(true)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
    }

    @Test
    void execute_ShouldCreateCommerce_WhenDataIsValid() {
        when(commerceRepository.findByTaxId(request.getTaxId())).thenReturn(Optional.empty());
        when(commerceRepository.save(any(AffiliatedBusinessEntity.class))).thenReturn(commerce);

        CommerceResponse response = createCommerceUseCase.execute(request);

        assertThat(response).isNotNull();
        assertThat(response.getId()).isEqualTo(1L);
        assertThat(response.getName()).isEqualTo("Restaurant El Portal");
        assertThat(response.getTaxId()).isEqualTo("12345678-9");
        assertThat(response.getRatePerHour()).isEqualByComparingTo(BigDecimal.valueOf(8.00));
        assertThat(response.getIsActive()).isTrue();

        verify(commerceRepository).findByTaxId(request.getTaxId());
        verify(commerceRepository).save(any(AffiliatedBusinessEntity.class));
    }

    @Test
    void execute_ShouldThrowBusinessRuleException_WhenTaxIdAlreadyExists() {
        when(commerceRepository.findByTaxId(request.getTaxId())).thenReturn(Optional.of(commerce));

        assertThatThrownBy(() -> createCommerceUseCase.execute(request))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("already exists");

        verify(commerceRepository).findByTaxId(request.getTaxId());
        verify(commerceRepository, never()).save(any(AffiliatedBusinessEntity.class));
    }
}
