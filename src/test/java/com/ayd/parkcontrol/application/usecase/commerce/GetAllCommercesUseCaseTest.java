package com.ayd.parkcontrol.application.usecase.commerce;

import com.ayd.parkcontrol.application.dto.response.commerce.CommerceResponse;
import com.ayd.parkcontrol.infrastructure.persistence.entity.AffiliatedBusinessEntity;
import com.ayd.parkcontrol.infrastructure.persistence.repository.JpaAffiliatedBusinessRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GetAllCommercesUseCaseTest {

    @Mock
    private JpaAffiliatedBusinessRepository commerceRepository;

    @InjectMocks
    private GetAllCommercesUseCase getAllCommercesUseCase;

    private List<AffiliatedBusinessEntity> commerceEntities;

    @BeforeEach
    void setUp() {
        AffiliatedBusinessEntity commerce1 = AffiliatedBusinessEntity.builder()
                .id(1L)
                .name("Commerce One")
                .taxId("12345678")
                .contactName("John Doe")
                .email("commerce1@test.com")
                .phone("12345678")
                .ratePerHour(BigDecimal.valueOf(10.00))
                .isActive(true)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        AffiliatedBusinessEntity commerce2 = AffiliatedBusinessEntity.builder()
                .id(2L)
                .name("Commerce Two")
                .taxId("87654321")
                .contactName("Jane Smith")
                .email("commerce2@test.com")
                .phone("87654321")
                .ratePerHour(BigDecimal.valueOf(15.00))
                .isActive(false)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        commerceEntities = Arrays.asList(commerce1, commerce2);
    }

    @Test
    void execute_shouldReturnPagedCommerces_whenCommercesExist() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<AffiliatedBusinessEntity> commercePage = new PageImpl<>(commerceEntities, pageable,
                commerceEntities.size());

        when(commerceRepository.findAll(any(Pageable.class))).thenReturn(commercePage);

        Page<CommerceResponse> result = getAllCommercesUseCase.execute(pageable);

        assertThat(result).isNotNull();
        assertThat(result.getTotalElements()).isEqualTo(2);
        assertThat(result.getContent()).hasSize(2);

        CommerceResponse first = result.getContent().get(0);
        assertThat(first.getName()).isEqualTo("Commerce One");
        assertThat(first.getEmail()).isEqualTo("commerce1@test.com");
        assertThat(first.getRatePerHour()).isEqualByComparingTo(BigDecimal.valueOf(10.00));
        assertThat(first.getIsActive()).isTrue();

        CommerceResponse second = result.getContent().get(1);
        assertThat(second.getName()).isEqualTo("Commerce Two");
        assertThat(second.getIsActive()).isFalse();
    }

    @Test
    void execute_shouldReturnEmptyPage_whenNoCommercesExist() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<AffiliatedBusinessEntity> emptyPage = new PageImpl<>(List.of(), pageable, 0);

        when(commerceRepository.findAll(any(Pageable.class))).thenReturn(emptyPage);

        Page<CommerceResponse> result = getAllCommercesUseCase.execute(pageable);

        assertThat(result).isNotNull();
        assertThat(result.getTotalElements()).isZero();
        assertThat(result.getContent()).isEmpty();
    }

    @Test
    void execute_shouldRespectPagination_whenFetchingCommerces() {
        Pageable pageable = PageRequest.of(1, 1);
        Page<AffiliatedBusinessEntity> singlePage = new PageImpl<>(List.of(commerceEntities.get(1)), pageable,
                commerceEntities.size());

        when(commerceRepository.findAll(any(Pageable.class))).thenReturn(singlePage);

        Page<CommerceResponse> result = getAllCommercesUseCase.execute(pageable);

        assertThat(result).isNotNull();
        assertThat(result.getTotalElements()).isEqualTo(2);
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getName()).isEqualTo("Commerce Two");
    }
}
