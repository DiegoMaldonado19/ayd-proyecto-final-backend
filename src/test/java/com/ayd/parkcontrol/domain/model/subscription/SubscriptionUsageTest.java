package com.ayd.parkcontrol.domain.model.subscription;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests unitarios para el modelo de dominio SubscriptionUsage.
 */
class SubscriptionUsageTest {

    @Test
    void builder_shouldCreateSubscriptionUsageCorrectly() {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime entryTime = now.minusHours(3);
        LocalDateTime exitTime = now;

        SubscriptionUsage usage = SubscriptionUsage.builder()
                .id(1L)
                .subscriptionId(100L)
                .ticketId(200L)
                .folio("FOL-12345")
                .branchName("Branch Central")
                .entryTime(entryTime)
                .exitTime(exitTime)
                .hoursConsumed(new BigDecimal("3.0"))
                .createdAt(now)
                .build();

        assertThat(usage).isNotNull();
        assertThat(usage.getId()).isEqualTo(1L);
        assertThat(usage.getSubscriptionId()).isEqualTo(100L);
        assertThat(usage.getTicketId()).isEqualTo(200L);
        assertThat(usage.getFolio()).isEqualTo("FOL-12345");
        assertThat(usage.getBranchName()).isEqualTo("Branch Central");
        assertThat(usage.getEntryTime()).isEqualTo(entryTime);
        assertThat(usage.getExitTime()).isEqualTo(exitTime);
        assertThat(usage.getHoursConsumed()).isEqualByComparingTo(new BigDecimal("3.0"));
        assertThat(usage.getCreatedAt()).isEqualTo(now);
    }

    @Test
    void noArgsConstructor_shouldCreateEmptyObject() {
        SubscriptionUsage usage = new SubscriptionUsage();
        assertThat(usage).isNotNull();
        assertThat(usage.getId()).isNull();
        assertThat(usage.getSubscriptionId()).isNull();
        assertThat(usage.getTicketId()).isNull();
        assertThat(usage.getFolio()).isNull();
        assertThat(usage.getBranchName()).isNull();
        assertThat(usage.getHoursConsumed()).isNull();
    }

    @Test
    void allArgsConstructor_shouldCreateObjectWithAllFields() {
        LocalDateTime now = LocalDateTime.now();
        SubscriptionUsage usage = new SubscriptionUsage(
                1L,
                100L,
                200L,
                "FOL-001",
                "Branch Norte",
                now.minusHours(2),
                now,
                new BigDecimal("2.0"),
                now);

        assertThat(usage.getId()).isEqualTo(1L);
        assertThat(usage.getSubscriptionId()).isEqualTo(100L);
        assertThat(usage.getTicketId()).isEqualTo(200L);
        assertThat(usage.getFolio()).isEqualTo("FOL-001");
        assertThat(usage.getBranchName()).isEqualTo("Branch Norte");
        assertThat(usage.getHoursConsumed()).isEqualByComparingTo(new BigDecimal("2.0"));
    }

    @Test
    void settersAndGetters_shouldWorkCorrectly() {
        SubscriptionUsage usage = new SubscriptionUsage();
        LocalDateTime now = LocalDateTime.now();

        usage.setId(5L);
        usage.setSubscriptionId(500L);
        usage.setTicketId(600L);
        usage.setFolio("FOL-999");
        usage.setBranchName("Branch Sur");
        usage.setEntryTime(now.minusHours(5));
        usage.setExitTime(now);
        usage.setHoursConsumed(new BigDecimal("5.0"));
        usage.setCreatedAt(now);

        assertThat(usage.getId()).isEqualTo(5L);
        assertThat(usage.getSubscriptionId()).isEqualTo(500L);
        assertThat(usage.getTicketId()).isEqualTo(600L);
        assertThat(usage.getFolio()).isEqualTo("FOL-999");
        assertThat(usage.getBranchName()).isEqualTo("Branch Sur");
        assertThat(usage.getEntryTime()).isEqualTo(now.minusHours(5));
        assertThat(usage.getExitTime()).isEqualTo(now);
        assertThat(usage.getHoursConsumed()).isEqualByComparingTo(new BigDecimal("5.0"));
        assertThat(usage.getCreatedAt()).isEqualTo(now);
    }

    @Test
    void equals_shouldReturnTrue_forSameValues() {
        SubscriptionUsage usage1 = SubscriptionUsage.builder()
                .id(1L)
                .subscriptionId(100L)
                .ticketId(200L)
                .folio("FOL-001")
                .hoursConsumed(new BigDecimal("3.0"))
                .build();

        SubscriptionUsage usage2 = SubscriptionUsage.builder()
                .id(1L)
                .subscriptionId(100L)
                .ticketId(200L)
                .folio("FOL-001")
                .hoursConsumed(new BigDecimal("3.0"))
                .build();

        assertThat(usage1).isEqualTo(usage2);
    }

    @Test
    void hashCode_shouldBeConsistent() {
        SubscriptionUsage usage1 = SubscriptionUsage.builder()
                .id(1L)
                .subscriptionId(100L)
                .ticketId(200L)
                .folio("FOL-001")
                .build();

        SubscriptionUsage usage2 = SubscriptionUsage.builder()
                .id(1L)
                .subscriptionId(100L)
                .ticketId(200L)
                .folio("FOL-001")
                .build();

        assertThat(usage1.hashCode()).isEqualTo(usage2.hashCode());
    }

    @Test
    void toString_shouldContainKeyFields() {
        SubscriptionUsage usage = SubscriptionUsage.builder()
                .id(1L)
                .subscriptionId(100L)
                .folio("FOL-12345")
                .hoursConsumed(new BigDecimal("3.0"))
                .build();

        String toString = usage.toString();
        assertThat(toString).contains("id=1");
        assertThat(toString).contains("subscriptionId=100");
        assertThat(toString).contains("folio=FOL-12345");
        assertThat(toString).contains("hoursConsumed=3.0");
    }
}
