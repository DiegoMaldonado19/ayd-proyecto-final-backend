package com.ayd.parkcontrol.domain.model.rate;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests unitarios para el modelo de dominio RateBranch.
 */
class RateBranchTest {

    @Test
    void builder_shouldCreateRateBranchCorrectly() {
        RateBranch rateBranch = RateBranch.builder()
                .branchId(1L)
                .branchName("Branch Central")
                .ratePerHour(new BigDecimal("15.50"))
                .isActive(true)
                .build();

        assertThat(rateBranch).isNotNull();
        assertThat(rateBranch.getBranchId()).isEqualTo(1L);
        assertThat(rateBranch.getBranchName()).isEqualTo("Branch Central");
        assertThat(rateBranch.getRatePerHour()).isEqualByComparingTo(new BigDecimal("15.50"));
        assertThat(rateBranch.getIsActive()).isTrue();
    }

    @Test
    void noArgsConstructor_shouldCreateEmptyObject() {
        RateBranch rateBranch = new RateBranch();
        assertThat(rateBranch).isNotNull();
        assertThat(rateBranch.getBranchId()).isNull();
        assertThat(rateBranch.getBranchName()).isNull();
        assertThat(rateBranch.getRatePerHour()).isNull();
        assertThat(rateBranch.getIsActive()).isNull();
    }

    @Test
    void allArgsConstructor_shouldCreateObjectWithAllFields() {
        RateBranch rateBranch = new RateBranch(
                2L,
                "Branch Norte",
                new BigDecimal("20.00"),
                false);

        assertThat(rateBranch.getBranchId()).isEqualTo(2L);
        assertThat(rateBranch.getBranchName()).isEqualTo("Branch Norte");
        assertThat(rateBranch.getRatePerHour()).isEqualByComparingTo(new BigDecimal("20.00"));
        assertThat(rateBranch.getIsActive()).isFalse();
    }

    @Test
    void settersAndGetters_shouldWorkCorrectly() {
        RateBranch rateBranch = new RateBranch();

        rateBranch.setBranchId(3L);
        rateBranch.setBranchName("Branch Sur");
        rateBranch.setRatePerHour(new BigDecimal("12.75"));
        rateBranch.setIsActive(true);

        assertThat(rateBranch.getBranchId()).isEqualTo(3L);
        assertThat(rateBranch.getBranchName()).isEqualTo("Branch Sur");
        assertThat(rateBranch.getRatePerHour()).isEqualByComparingTo(new BigDecimal("12.75"));
        assertThat(rateBranch.getIsActive()).isTrue();
    }

    @Test
    void equals_shouldReturnTrue_forSameValues() {
        RateBranch branch1 = RateBranch.builder()
                .branchId(1L)
                .branchName("Branch A")
                .ratePerHour(new BigDecimal("10.00"))
                .isActive(true)
                .build();

        RateBranch branch2 = RateBranch.builder()
                .branchId(1L)
                .branchName("Branch A")
                .ratePerHour(new BigDecimal("10.00"))
                .isActive(true)
                .build();

        assertThat(branch1).isEqualTo(branch2);
    }

    @Test
    void hashCode_shouldBeConsistent() {
        RateBranch branch1 = RateBranch.builder()
                .branchId(1L)
                .branchName("Branch A")
                .ratePerHour(new BigDecimal("10.00"))
                .build();

        RateBranch branch2 = RateBranch.builder()
                .branchId(1L)
                .branchName("Branch A")
                .ratePerHour(new BigDecimal("10.00"))
                .build();

        assertThat(branch1.hashCode()).isEqualTo(branch2.hashCode());
    }

    @Test
    void toString_shouldContainAllFields() {
        RateBranch rateBranch = RateBranch.builder()
                .branchId(1L)
                .branchName("Branch Central")
                .ratePerHour(new BigDecimal("15.50"))
                .isActive(true)
                .build();

        String toString = rateBranch.toString();
        assertThat(toString).contains("branchId=1");
        assertThat(toString).contains("branchName=Branch Central");
        assertThat(toString).contains("ratePerHour=15.50");
        assertThat(toString).contains("isActive=true");
    }
}
