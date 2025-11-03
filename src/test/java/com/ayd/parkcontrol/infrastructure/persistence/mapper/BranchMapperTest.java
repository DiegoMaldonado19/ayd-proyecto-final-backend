package com.ayd.parkcontrol.infrastructure.persistence.mapper;

import com.ayd.parkcontrol.domain.model.branch.Branch;
import com.ayd.parkcontrol.infrastructure.persistence.entity.BranchEntity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.LocalTime;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
class BranchMapperTest {

    private BranchMapper mapper;

    @BeforeEach
    void setUp() {
        mapper = new BranchMapper();
    }

    @Test
    void toEntity_shouldReturnNull_whenBranchIsNull() {
        BranchEntity result = mapper.toEntity(null);

        assertThat(result).isNull();
    }

    @Test
    void toEntity_shouldMapCorrectly_whenBranchIsValid() {
        LocalDateTime now = LocalDateTime.now();
        LocalTime openingTime = LocalTime.of(6, 0);
        LocalTime closingTime = LocalTime.of(22, 0);

        Branch branch = Branch.builder()
                .id(1L)
                .name("Sucursal Centro")
                .address("5ta Avenida 10-20 Zona 1, Ciudad de Guatemala")
                .openingTime(openingTime)
                .closingTime(closingTime)
                .capacity2r(50)
                .capacity4r(100)
                .ratePerHour(new BigDecimal("15.00"))
                .isActive(true)
                .createdAt(now)
                .updatedAt(now)
                .build();

        BranchEntity result = mapper.toEntity(branch);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getName()).isEqualTo("Sucursal Centro");
        assertThat(result.getAddress()).isEqualTo("5ta Avenida 10-20 Zona 1, Ciudad de Guatemala");
        assertThat(result.getOpeningTime()).isEqualTo(openingTime);
        assertThat(result.getClosingTime()).isEqualTo(closingTime);
        assertThat(result.getCapacity2r()).isEqualTo(50);
        assertThat(result.getCapacity4r()).isEqualTo(100);
        assertThat(result.getRatePerHour()).isEqualByComparingTo(new BigDecimal("15.00"));
        assertThat(result.getIsActive()).isTrue();
        assertThat(result.getCreatedAt()).isEqualTo(now);
        assertThat(result.getUpdatedAt()).isEqualTo(now);
    }

    @Test
    void toDomain_shouldReturnNull_whenEntityIsNull() {
        Branch result = mapper.toDomain(null);

        assertThat(result).isNull();
    }

    @Test
    void toDomain_shouldMapCorrectly_whenEntityIsValid() {
        LocalDateTime now = LocalDateTime.now();
        LocalTime openingTime = LocalTime.of(7, 0);
        LocalTime closingTime = LocalTime.of(21, 0);

        BranchEntity entity = BranchEntity.builder()
                .id(2L)
                .name("Sucursal Plaza")
                .address("Avenida Las Americas 15-30 Zona 13")
                .openingTime(openingTime)
                .closingTime(closingTime)
                .capacity2r(30)
                .capacity4r(80)
                .ratePerHour(new BigDecimal("12.00"))
                .isActive(true)
                .createdAt(now)
                .updatedAt(now)
                .build();

        Branch result = mapper.toDomain(entity);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(2L);
        assertThat(result.getName()).isEqualTo("Sucursal Plaza");
        assertThat(result.getAddress()).isEqualTo("Avenida Las Americas 15-30 Zona 13");
        assertThat(result.getOpeningTime()).isEqualTo(openingTime);
        assertThat(result.getClosingTime()).isEqualTo(closingTime);
        assertThat(result.getCapacity2r()).isEqualTo(30);
        assertThat(result.getCapacity4r()).isEqualTo(80);
        assertThat(result.getRatePerHour()).isEqualByComparingTo(new BigDecimal("12.00"));
        assertThat(result.getIsActive()).isTrue();
        assertThat(result.getCreatedAt()).isEqualTo(now);
        assertThat(result.getUpdatedAt()).isEqualTo(now);
    }

    @Test
    void toEntity_shouldHandleNullRatePerHour() {
        LocalTime openingTime = LocalTime.of(6, 0);
        LocalTime closingTime = LocalTime.of(23, 0);

        Branch branch = Branch.builder()
                .id(3L)
                .name("Sucursal Norte")
                .address("Calzada San Juan 8-50 Zona 7, Mixco")
                .openingTime(openingTime)
                .closingTime(closingTime)
                .capacity2r(40)
                .capacity4r(120)
                .ratePerHour(null)
                .isActive(true)
                .createdAt(LocalDateTime.now())
                .updatedAt(null)
                .build();

        BranchEntity result = mapper.toEntity(branch);

        assertThat(result).isNotNull();
        assertThat(result.getRatePerHour()).isNull();
        assertThat(result.getUpdatedAt()).isNull();
    }

    @Test
    void toDomain_shouldHandleNullRatePerHour() {
        LocalTime openingTime = LocalTime.of(8, 0);
        LocalTime closingTime = LocalTime.of(20, 0);

        BranchEntity entity = BranchEntity.builder()
                .id(4L)
                .name("Sucursal Test")
                .address("Test Address")
                .openingTime(openingTime)
                .closingTime(closingTime)
                .capacity2r(25)
                .capacity4r(60)
                .ratePerHour(null)
                .isActive(false)
                .createdAt(LocalDateTime.now())
                .updatedAt(null)
                .build();

        Branch result = mapper.toDomain(entity);

        assertThat(result).isNotNull();
        assertThat(result.getRatePerHour()).isNull();
        assertThat(result.getIsActive()).isFalse();
        assertThat(result.getUpdatedAt()).isNull();
    }

    @Test
    void bidirectionalMapping_shouldPreserveData() {
        LocalDateTime now = LocalDateTime.now();
        LocalTime openingTime = LocalTime.of(6, 0);
        LocalTime closingTime = LocalTime.of(22, 0);

        Branch originalBranch = Branch.builder()
                .id(5L)
                .name("Sucursal Bidirectional")
                .address("Test Bidirectional Address")
                .openingTime(openingTime)
                .closingTime(closingTime)
                .capacity2r(35)
                .capacity4r(90)
                .ratePerHour(new BigDecimal("14.50"))
                .isActive(true)
                .createdAt(now)
                .updatedAt(now)
                .build();

        BranchEntity entity = mapper.toEntity(originalBranch);
        Branch resultBranch = mapper.toDomain(entity);

        assertThat(resultBranch).isNotNull();
        assertThat(resultBranch.getId()).isEqualTo(originalBranch.getId());
        assertThat(resultBranch.getName()).isEqualTo(originalBranch.getName());
        assertThat(resultBranch.getAddress()).isEqualTo(originalBranch.getAddress());
        assertThat(resultBranch.getOpeningTime()).isEqualTo(originalBranch.getOpeningTime());
        assertThat(resultBranch.getClosingTime()).isEqualTo(originalBranch.getClosingTime());
        assertThat(resultBranch.getCapacity2r()).isEqualTo(originalBranch.getCapacity2r());
        assertThat(resultBranch.getCapacity4r()).isEqualTo(originalBranch.getCapacity4r());
        assertThat(resultBranch.getRatePerHour()).isEqualByComparingTo(originalBranch.getRatePerHour());
        assertThat(resultBranch.getIsActive()).isEqualTo(originalBranch.getIsActive());
    }

    @Test
    void toEntity_shouldHandleInactiveBranch() {
        Branch branch = Branch.builder()
                .id(6L)
                .name("Inactive Branch")
                .address("Inactive Address")
                .openingTime(LocalTime.of(9, 0))
                .closingTime(LocalTime.of(18, 0))
                .capacity2r(20)
                .capacity4r(50)
                .ratePerHour(new BigDecimal("10.00"))
                .isActive(false)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        BranchEntity result = mapper.toEntity(branch);

        assertThat(result).isNotNull();
        assertThat(result.getIsActive()).isFalse();
    }

    @Test
    void toDomain_shouldHandleDifferentCapacities() {
        BranchEntity entity = BranchEntity.builder()
                .id(7L)
                .name("Large Branch")
                .address("Large Branch Address")
                .openingTime(LocalTime.of(5, 0))
                .closingTime(LocalTime.of(23, 59))
                .capacity2r(100)
                .capacity4r(200)
                .ratePerHour(new BigDecimal("20.00"))
                .isActive(true)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        Branch result = mapper.toDomain(entity);

        assertThat(result).isNotNull();
        assertThat(result.getCapacity2r()).isEqualTo(100);
        assertThat(result.getCapacity4r()).isEqualTo(200);
    }
}
