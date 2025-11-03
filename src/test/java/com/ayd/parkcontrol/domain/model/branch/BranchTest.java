package com.ayd.parkcontrol.domain.model.branch;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.LocalTime;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests unitarios para el modelo de dominio Branch.
 */
class BranchTest {

    @Test
    void builder_shouldCreateBranchCorrectly() {
        LocalDateTime now = LocalDateTime.now();
        Branch branch = Branch.builder()
                .id(1L)
                .name("Branch Central")
                .address("Av. Principal 123")
                .openingTime(LocalTime.of(8, 0))
                .closingTime(LocalTime.of(20, 0))
                .capacity2r(50)
                .capacity4r(100)
                .ratePerHour(new BigDecimal("15.00"))
                .isActive(true)
                .createdAt(now)
                .updatedAt(now)
                .build();

        assertThat(branch).isNotNull();
        assertThat(branch.getId()).isEqualTo(1L);
        assertThat(branch.getName()).isEqualTo("Branch Central");
        assertThat(branch.getAddress()).isEqualTo("Av. Principal 123");
        assertThat(branch.getOpeningTime()).isEqualTo(LocalTime.of(8, 0));
        assertThat(branch.getClosingTime()).isEqualTo(LocalTime.of(20, 0));
        assertThat(branch.getCapacity2r()).isEqualTo(50);
        assertThat(branch.getCapacity4r()).isEqualTo(100);
        assertThat(branch.getRatePerHour()).isEqualByComparingTo(new BigDecimal("15.00"));
        assertThat(branch.getIsActive()).isTrue();
        assertThat(branch.getCreatedAt()).isEqualTo(now);
        assertThat(branch.getUpdatedAt()).isEqualTo(now);
    }

    @Test
    void noArgsConstructor_shouldCreateEmptyObject() {
        Branch branch = new Branch();
        assertThat(branch).isNotNull();
        assertThat(branch.getId()).isNull();
        assertThat(branch.getName()).isNull();
        assertThat(branch.getAddress()).isNull();
        assertThat(branch.getOpeningTime()).isNull();
        assertThat(branch.getClosingTime()).isNull();
        assertThat(branch.getCapacity2r()).isNull();
        assertThat(branch.getCapacity4r()).isNull();
        assertThat(branch.getRatePerHour()).isNull();
        assertThat(branch.getIsActive()).isNull();
    }

    @Test
    void allArgsConstructor_shouldCreateObjectWithAllFields() {
        LocalDateTime now = LocalDateTime.now();
        Branch branch = new Branch(
                2L,
                "Branch Norte",
                "Calle 456",
                LocalTime.of(7, 0),
                LocalTime.of(22, 0),
                30,
                80,
                new BigDecimal("20.00"),
                false,
                now,
                now);

        assertThat(branch.getId()).isEqualTo(2L);
        assertThat(branch.getName()).isEqualTo("Branch Norte");
        assertThat(branch.getAddress()).isEqualTo("Calle 456");
        assertThat(branch.getOpeningTime()).isEqualTo(LocalTime.of(7, 0));
        assertThat(branch.getClosingTime()).isEqualTo(LocalTime.of(22, 0));
        assertThat(branch.getCapacity2r()).isEqualTo(30);
        assertThat(branch.getCapacity4r()).isEqualTo(80);
        assertThat(branch.getRatePerHour()).isEqualByComparingTo(new BigDecimal("20.00"));
        assertThat(branch.getIsActive()).isFalse();
    }

    @Test
    void settersAndGetters_shouldWorkCorrectly() {
        Branch branch = new Branch();
        LocalDateTime now = LocalDateTime.now();

        branch.setId(3L);
        branch.setName("Branch Sur");
        branch.setAddress("Avenida Sur 789");
        branch.setOpeningTime(LocalTime.of(6, 0));
        branch.setClosingTime(LocalTime.of(23, 0));
        branch.setCapacity2r(40);
        branch.setCapacity4r(90);
        branch.setRatePerHour(new BigDecimal("18.50"));
        branch.setIsActive(true);
        branch.setCreatedAt(now);
        branch.setUpdatedAt(now);

        assertThat(branch.getId()).isEqualTo(3L);
        assertThat(branch.getName()).isEqualTo("Branch Sur");
        assertThat(branch.getAddress()).isEqualTo("Avenida Sur 789");
        assertThat(branch.getOpeningTime()).isEqualTo(LocalTime.of(6, 0));
        assertThat(branch.getClosingTime()).isEqualTo(LocalTime.of(23, 0));
        assertThat(branch.getCapacity2r()).isEqualTo(40);
        assertThat(branch.getCapacity4r()).isEqualTo(90);
        assertThat(branch.getRatePerHour()).isEqualByComparingTo(new BigDecimal("18.50"));
        assertThat(branch.getIsActive()).isTrue();
        assertThat(branch.getCreatedAt()).isEqualTo(now);
        assertThat(branch.getUpdatedAt()).isEqualTo(now);
    }

    @Test
    void equals_shouldReturnTrue_forSameValues() {
        Branch branch1 = Branch.builder()
                .id(1L)
                .name("Branch Central")
                .address("Av. Principal 123")
                .capacity2r(50)
                .capacity4r(100)
                .build();

        Branch branch2 = Branch.builder()
                .id(1L)
                .name("Branch Central")
                .address("Av. Principal 123")
                .capacity2r(50)
                .capacity4r(100)
                .build();

        assertThat(branch1).isEqualTo(branch2);
    }

    @Test
    void hashCode_shouldBeConsistent() {
        Branch branch1 = Branch.builder()
                .id(1L)
                .name("Branch Central")
                .build();

        Branch branch2 = Branch.builder()
                .id(1L)
                .name("Branch Central")
                .build();

        assertThat(branch1.hashCode()).isEqualTo(branch2.hashCode());
    }

    @Test
    void toString_shouldContainKeyFields() {
        Branch branch = Branch.builder()
                .id(1L)
                .name("Branch Central")
                .address("Av. Principal 123")
                .capacity2r(50)
                .capacity4r(100)
                .ratePerHour(new BigDecimal("15.00"))
                .isActive(true)
                .build();

        String toString = branch.toString();
        assertThat(toString).contains("id=1");
        assertThat(toString).contains("name=Branch Central");
        assertThat(toString).contains("address=Av. Principal 123");
        assertThat(toString).contains("capacity2r=50");
        assertThat(toString).contains("capacity4r=100");
        assertThat(toString).contains("ratePerHour=15.00");
        assertThat(toString).contains("isActive=true");
    }
}
