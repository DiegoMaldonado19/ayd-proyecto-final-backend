package com.ayd.parkcontrol.infrastructure.persistence.mapper;

import com.ayd.parkcontrol.domain.model.subscription.SubscriptionOverage;
import com.ayd.parkcontrol.domain.model.subscription.SubscriptionUsage;
import com.ayd.parkcontrol.infrastructure.persistence.entity.BranchEntity;
import com.ayd.parkcontrol.infrastructure.persistence.entity.SubscriptionOverageEntity;
import com.ayd.parkcontrol.infrastructure.persistence.entity.SubscriptionUsageEntity;
import com.ayd.parkcontrol.infrastructure.persistence.entity.TicketEntity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
class SubscriptionUsageMapperTest {

    private SubscriptionUsageMapper mapper;

    @BeforeEach
    void setUp() {
        mapper = new SubscriptionUsageMapper();
    }

    @Test
    void toDomain_shouldReturnNull_whenEntityIsNull() {
        SubscriptionUsage result = mapper.toDomain(null);

        assertThat(result).isNull();
    }

    @Test
    void toDomain_shouldMapCorrectly_whenEntityIsValid() {
        LocalDateTime now = LocalDateTime.now();
        BranchEntity branch = BranchEntity.builder()
                .id(1L)
                .name("Sucursal Centro")
                .build();

        SubscriptionUsageEntity entity = SubscriptionUsageEntity.builder()
                .id(1L)
                .subscriptionId(1L)
                .folio("TICKET-001")
                .branch(branch)
                .entryTime(now)
                .exitTime(now.plusHours(3))
                .totalHours(new BigDecimal("3.00"))
                .createdAt(now)
                .build();

        SubscriptionUsage result = mapper.toDomain(entity);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getSubscriptionId()).isEqualTo(1L);
        assertThat(result.getTicketId()).isEqualTo(1L);
        assertThat(result.getFolio()).isEqualTo("TICKET-001");
        assertThat(result.getBranchName()).isEqualTo("Sucursal Centro");
        assertThat(result.getEntryTime()).isEqualTo(now);
        assertThat(result.getExitTime()).isEqualTo(now.plusHours(3));
        assertThat(result.getHoursConsumed()).isEqualByComparingTo(new BigDecimal("3.00"));
        assertThat(result.getCreatedAt()).isEqualTo(now);
    }

    @Test
    void toDomain_shouldHandleNullBranch() {
        LocalDateTime now = LocalDateTime.now();
        SubscriptionUsageEntity entity = SubscriptionUsageEntity.builder()
                .id(2L)
                .subscriptionId(2L)
                .folio("TICKET-002")
                .branch(null)
                .entryTime(now)
                .exitTime(now.plusHours(5))
                .totalHours(new BigDecimal("5.00"))
                .createdAt(now)
                .build();

        SubscriptionUsage result = mapper.toDomain(entity);

        assertThat(result).isNotNull();
        assertThat(result.getBranchName()).isNull();
    }

    @Test
    void toOverageDomain_shouldReturnNull_whenEntityIsNull() {
        SubscriptionOverage result = mapper.toOverageDomain(null);

        assertThat(result).isNull();
    }

    @Test
    void toOverageDomain_shouldMapCorrectly_whenEntityIsValid() {
        LocalDateTime now = LocalDateTime.now();
        TicketEntity ticket = TicketEntity.builder()
                .id(1L)
                .folio("TICKET-OVG-001")
                .build();

        SubscriptionOverageEntity entity = SubscriptionOverageEntity.builder()
                .id(1L)
                .subscriptionId(1L)
                .ticketId(1L)
                .ticket(ticket)
                .overageHours(new BigDecimal("5.50"))
                .chargedAmount(new BigDecimal("66.00"))
                .appliedRate(new BigDecimal("12.00"))
                .chargedAt(now)
                .createdAt(now)
                .build();

        SubscriptionOverage result = mapper.toOverageDomain(entity);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getSubscriptionId()).isEqualTo(1L);
        assertThat(result.getTicketId()).isEqualTo(1L);
        assertThat(result.getFolio()).isEqualTo("TICKET-OVG-001");
        assertThat(result.getBranchName()).isNull();
        assertThat(result.getOverageHours()).isEqualByComparingTo(new BigDecimal("5.50"));
        assertThat(result.getChargedAmount()).isEqualByComparingTo(new BigDecimal("66.00"));
        assertThat(result.getAppliedRate()).isEqualByComparingTo(new BigDecimal("12.00"));
        assertThat(result.getChargedAt()).isEqualTo(now);
        assertThat(result.getCreatedAt()).isEqualTo(now);
    }

    @Test
    void toOverageDomain_shouldHandleNullTicket() {
        LocalDateTime now = LocalDateTime.now();
        SubscriptionOverageEntity entity = SubscriptionOverageEntity.builder()
                .id(2L)
                .subscriptionId(2L)
                .ticketId(2L)
                .ticket(null)
                .overageHours(new BigDecimal("2.25"))
                .chargedAmount(new BigDecimal("27.00"))
                .appliedRate(new BigDecimal("12.00"))
                .chargedAt(now)
                .createdAt(now)
                .build();

        SubscriptionOverage result = mapper.toOverageDomain(entity);

        assertThat(result).isNotNull();
        assertThat(result.getFolio()).isNull();
    }

    @Test
    void toDomain_shouldHandleMultipleUsages() {
        LocalDateTime now = LocalDateTime.now();
        BranchEntity branch1 = BranchEntity.builder()
                .id(1L)
                .name("Sucursal Centro")
                .build();

        BranchEntity branch2 = BranchEntity.builder()
                .id(2L)
                .name("Sucursal Plaza")
                .build();

        SubscriptionUsageEntity usage1 = SubscriptionUsageEntity.builder()
                .id(1L)
                .subscriptionId(1L)
                .folio("TICKET-001")
                .branch(branch1)
                .entryTime(now)
                .exitTime(now.plusHours(4))
                .totalHours(new BigDecimal("4.00"))
                .createdAt(now)
                .build();

        SubscriptionUsageEntity usage2 = SubscriptionUsageEntity.builder()
                .id(2L)
                .subscriptionId(1L)
                .folio("TICKET-002")
                .branch(branch2)
                .entryTime(now.plusDays(1))
                .exitTime(now.plusDays(1).plusHours(2))
                .totalHours(new BigDecimal("2.00"))
                .createdAt(now.plusDays(1))
                .build();

        SubscriptionUsage result1 = mapper.toDomain(usage1);
        SubscriptionUsage result2 = mapper.toDomain(usage2);

        assertThat(result1.getBranchName()).isEqualTo("Sucursal Centro");
        assertThat(result2.getBranchName()).isEqualTo("Sucursal Plaza");
        assertThat(result1.getHoursConsumed()).isEqualByComparingTo(new BigDecimal("4.00"));
        assertThat(result2.getHoursConsumed()).isEqualByComparingTo(new BigDecimal("2.00"));
    }

    @Test
    void toOverageDomain_shouldHandleMultipleOverages() {
        LocalDateTime now = LocalDateTime.now();
        TicketEntity ticket1 = TicketEntity.builder()
                .id(1L)
                .folio("TICKET-OVG-001")
                .build();

        TicketEntity ticket2 = TicketEntity.builder()
                .id(2L)
                .folio("TICKET-OVG-002")
                .build();

        SubscriptionOverageEntity overage1 = SubscriptionOverageEntity.builder()
                .id(1L)
                .subscriptionId(1L)
                .ticketId(1L)
                .ticket(ticket1)
                .overageHours(new BigDecimal("3.00"))
                .chargedAmount(new BigDecimal("36.00"))
                .appliedRate(new BigDecimal("12.00"))
                .chargedAt(now)
                .createdAt(now)
                .build();

        SubscriptionOverageEntity overage2 = SubscriptionOverageEntity.builder()
                .id(2L)
                .subscriptionId(1L)
                .ticketId(2L)
                .ticket(ticket2)
                .overageHours(new BigDecimal("1.50"))
                .chargedAmount(new BigDecimal("18.00"))
                .appliedRate(new BigDecimal("12.00"))
                .chargedAt(now.plusDays(1))
                .createdAt(now.plusDays(1))
                .build();

        SubscriptionOverage result1 = mapper.toOverageDomain(overage1);
        SubscriptionOverage result2 = mapper.toOverageDomain(overage2);

        assertThat(result1.getFolio()).isEqualTo("TICKET-OVG-001");
        assertThat(result2.getFolio()).isEqualTo("TICKET-OVG-002");
        assertThat(result1.getOverageHours()).isEqualByComparingTo(new BigDecimal("3.00"));
        assertThat(result2.getOverageHours()).isEqualByComparingTo(new BigDecimal("1.50"));
    }
}
