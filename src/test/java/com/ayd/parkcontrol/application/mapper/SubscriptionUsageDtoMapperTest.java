package com.ayd.parkcontrol.application.mapper;

import com.ayd.parkcontrol.application.dto.response.subscription.SubscriptionOverageResponse;
import com.ayd.parkcontrol.application.dto.response.subscription.SubscriptionUsageResponse;
import com.ayd.parkcontrol.domain.model.subscription.SubscriptionOverage;
import com.ayd.parkcontrol.domain.model.subscription.SubscriptionUsage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

class SubscriptionUsageDtoMapperTest {

    private SubscriptionUsageDtoMapper mapper;

    @BeforeEach
    void setUp() {
        mapper = new SubscriptionUsageDtoMapper();
    }

    @Test
    void toResponse_shouldMapCorrectly() {
        LocalDateTime now = LocalDateTime.now();

        SubscriptionUsage domain = SubscriptionUsage.builder()
                .subscriptionId(1L)
                .ticketId(100L)
                .folio("FOL-001")
                .branchName("Sucursal Central")
                .entryTime(now.minusHours(3))
                .exitTime(now)
                .hoursConsumed(new BigDecimal("3.00"))
                .createdAt(now)
                .build();

        SubscriptionUsageResponse result = mapper.toResponse(domain);

        assertThat(result).isNotNull();
        assertThat(result.getSubscriptionId()).isEqualTo(1L);
        assertThat(result.getTicketId()).isEqualTo(100L);
        assertThat(result.getFolio()).isEqualTo("FOL-001");
        assertThat(result.getBranchName()).isEqualTo("Sucursal Central");
        assertThat(result.getEntryTime()).isEqualTo(now.minusHours(3));
        assertThat(result.getExitTime()).isEqualTo(now);
        assertThat(result.getHoursConsumed()).isEqualTo(new BigDecimal("3.00"));
        assertThat(result.getCreatedAt()).isEqualTo(now);
    }

    @Test
    void toResponse_withNullInput_shouldReturnNull() {
        SubscriptionUsageResponse result = mapper.toResponse(null);
        assertThat(result).isNull();
    }

    @Test
    void toOverageResponse_shouldMapCorrectly() {
        LocalDateTime now = LocalDateTime.now();

        SubscriptionOverage domain = SubscriptionOverage.builder()
                .id(1L)
                .subscriptionId(10L)
                .ticketId(200L)
                .folio("FOL-002")
                .branchName("Sucursal Norte")
                .overageHours(new BigDecimal("2.50"))
                .appliedRate(new BigDecimal("15.00"))
                .chargedAmount(new BigDecimal("37.50"))
                .chargedAt(now)
                .createdAt(now)
                .build();

        SubscriptionOverageResponse result = mapper.toOverageResponse(domain);

        assertThat(result).isNotNull();
        assertThat(result.getOverageId()).isEqualTo(1L);
        assertThat(result.getSubscriptionId()).isEqualTo(10L);
        assertThat(result.getTicketId()).isEqualTo(200L);
        assertThat(result.getFolio()).isEqualTo("FOL-002");
        assertThat(result.getBranchName()).isEqualTo("Sucursal Norte");
        assertThat(result.getOverageHours()).isEqualTo(new BigDecimal("2.50"));
        assertThat(result.getRateApplied()).isEqualTo(new BigDecimal("15.00"));
        assertThat(result.getAmountCharged()).isEqualTo(new BigDecimal("37.50"));
        assertThat(result.getChargeDate()).isEqualTo(now);
        assertThat(result.getCreatedAt()).isEqualTo(now);
    }

    @Test
    void toOverageResponse_withNullInput_shouldReturnNull() {
        SubscriptionOverageResponse result = mapper.toOverageResponse(null);
        assertThat(result).isNull();
    }
}
