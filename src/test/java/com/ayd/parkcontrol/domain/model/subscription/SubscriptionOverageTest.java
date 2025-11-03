package com.ayd.parkcontrol.domain.model.subscription;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests unitarios para el modelo de dominio SubscriptionOverage.
 */
class SubscriptionOverageTest {

    @Test
    void builder_shouldCreateSubscriptionOverageCorrectly() {
        LocalDateTime now = LocalDateTime.now();

        SubscriptionOverage overage = SubscriptionOverage.builder()
                .id(1L)
                .subscriptionId(100L)
                .ticketId(200L)
                .folio("FOL-12345")
                .branchName("Branch Central")
                .overageHours(new BigDecimal("2.5"))
                .chargedAmount(new BigDecimal("37.50"))
                .appliedRate(new BigDecimal("15.00"))
                .chargedAt(now)
                .createdAt(now)
                .build();

        assertThat(overage).isNotNull();
        assertThat(overage.getId()).isEqualTo(1L);
        assertThat(overage.getSubscriptionId()).isEqualTo(100L);
        assertThat(overage.getTicketId()).isEqualTo(200L);
        assertThat(overage.getFolio()).isEqualTo("FOL-12345");
        assertThat(overage.getBranchName()).isEqualTo("Branch Central");
        assertThat(overage.getOverageHours()).isEqualByComparingTo(new BigDecimal("2.5"));
        assertThat(overage.getChargedAmount()).isEqualByComparingTo(new BigDecimal("37.50"));
        assertThat(overage.getAppliedRate()).isEqualByComparingTo(new BigDecimal("15.00"));
        assertThat(overage.getChargedAt()).isEqualTo(now);
        assertThat(overage.getCreatedAt()).isEqualTo(now);
    }

    @Test
    void noArgsConstructor_shouldCreateEmptyObject() {
        SubscriptionOverage overage = new SubscriptionOverage();
        assertThat(overage).isNotNull();
        assertThat(overage.getId()).isNull();
        assertThat(overage.getSubscriptionId()).isNull();
        assertThat(overage.getTicketId()).isNull();
        assertThat(overage.getFolio()).isNull();
        assertThat(overage.getBranchName()).isNull();
        assertThat(overage.getOverageHours()).isNull();
        assertThat(overage.getChargedAmount()).isNull();
        assertThat(overage.getAppliedRate()).isNull();
    }

    @Test
    void allArgsConstructor_shouldCreateObjectWithAllFields() {
        LocalDateTime now = LocalDateTime.now();
        SubscriptionOverage overage = new SubscriptionOverage(
                1L,
                100L,
                200L,
                "FOL-001",
                "Branch Norte",
                new BigDecimal("1.5"),
                new BigDecimal("22.50"),
                new BigDecimal("15.00"),
                now,
                now);

        assertThat(overage.getId()).isEqualTo(1L);
        assertThat(overage.getSubscriptionId()).isEqualTo(100L);
        assertThat(overage.getTicketId()).isEqualTo(200L);
        assertThat(overage.getFolio()).isEqualTo("FOL-001");
        assertThat(overage.getBranchName()).isEqualTo("Branch Norte");
        assertThat(overage.getOverageHours()).isEqualByComparingTo(new BigDecimal("1.5"));
        assertThat(overage.getChargedAmount()).isEqualByComparingTo(new BigDecimal("22.50"));
        assertThat(overage.getAppliedRate()).isEqualByComparingTo(new BigDecimal("15.00"));
    }

    @Test
    void settersAndGetters_shouldWorkCorrectly() {
        SubscriptionOverage overage = new SubscriptionOverage();
        LocalDateTime now = LocalDateTime.now();

        overage.setId(5L);
        overage.setSubscriptionId(500L);
        overage.setTicketId(600L);
        overage.setFolio("FOL-999");
        overage.setBranchName("Branch Sur");
        overage.setOverageHours(new BigDecimal("3.0"));
        overage.setChargedAmount(new BigDecimal("45.00"));
        overage.setAppliedRate(new BigDecimal("15.00"));
        overage.setChargedAt(now);
        overage.setCreatedAt(now);

        assertThat(overage.getId()).isEqualTo(5L);
        assertThat(overage.getSubscriptionId()).isEqualTo(500L);
        assertThat(overage.getTicketId()).isEqualTo(600L);
        assertThat(overage.getFolio()).isEqualTo("FOL-999");
        assertThat(overage.getBranchName()).isEqualTo("Branch Sur");
        assertThat(overage.getOverageHours()).isEqualByComparingTo(new BigDecimal("3.0"));
        assertThat(overage.getChargedAmount()).isEqualByComparingTo(new BigDecimal("45.00"));
        assertThat(overage.getAppliedRate()).isEqualByComparingTo(new BigDecimal("15.00"));
        assertThat(overage.getChargedAt()).isEqualTo(now);
        assertThat(overage.getCreatedAt()).isEqualTo(now);
    }

    @Test
    void equals_shouldReturnTrue_forSameValues() {
        SubscriptionOverage overage1 = SubscriptionOverage.builder()
                .id(1L)
                .subscriptionId(100L)
                .ticketId(200L)
                .folio("FOL-001")
                .overageHours(new BigDecimal("2.5"))
                .chargedAmount(new BigDecimal("37.50"))
                .build();

        SubscriptionOverage overage2 = SubscriptionOverage.builder()
                .id(1L)
                .subscriptionId(100L)
                .ticketId(200L)
                .folio("FOL-001")
                .overageHours(new BigDecimal("2.5"))
                .chargedAmount(new BigDecimal("37.50"))
                .build();

        assertThat(overage1).isEqualTo(overage2);
    }

    @Test
    void hashCode_shouldBeConsistent() {
        SubscriptionOverage overage1 = SubscriptionOverage.builder()
                .id(1L)
                .subscriptionId(100L)
                .ticketId(200L)
                .folio("FOL-001")
                .build();

        SubscriptionOverage overage2 = SubscriptionOverage.builder()
                .id(1L)
                .subscriptionId(100L)
                .ticketId(200L)
                .folio("FOL-001")
                .build();

        assertThat(overage1.hashCode()).isEqualTo(overage2.hashCode());
    }

    @Test
    void toString_shouldContainKeyFields() {
        SubscriptionOverage overage = SubscriptionOverage.builder()
                .id(1L)
                .subscriptionId(100L)
                .folio("FOL-12345")
                .overageHours(new BigDecimal("2.5"))
                .chargedAmount(new BigDecimal("37.50"))
                .appliedRate(new BigDecimal("15.00"))
                .build();

        String toString = overage.toString();
        assertThat(toString).contains("id=1");
        assertThat(toString).contains("subscriptionId=100");
        assertThat(toString).contains("folio=FOL-12345");
        assertThat(toString).contains("overageHours=2.5");
        assertThat(toString).contains("chargedAmount=37.50");
        assertThat(toString).contains("appliedRate=15.00");
    }
}
