package com.ayd.parkcontrol.infrastructure.persistence.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "fleet_companies")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FleetCompanyEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "name", nullable = false, length = 200)
    private String name;

    @Column(name = "tax_id", nullable = false, length = 50, unique = true)
    private String taxId;

    @Column(name = "contact_name", length = 200)
    private String contactName;

    @Column(name = "corporate_email", nullable = false, length = 255)
    private String corporateEmail;

    @Column(name = "phone", length = 20)
    private String phone;

    @Column(name = "corporate_discount_percentage", nullable = false, precision = 5, scale = 2)
    private BigDecimal corporateDiscountPercentage;

    @Column(name = "plate_limit", nullable = false)
    private Integer plateLimit;

    @Column(name = "billing_period", nullable = false, length = 20)
    private String billingPeriod;

    @Builder.Default
    @Column(name = "months_unpaid", nullable = false)
    private Integer monthsUnpaid = 0;

    @Builder.Default
    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;

    @Column(name = "admin_user_id")
    private Long adminUserId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "admin_user_id", insertable = false, updatable = false)
    private UserEntity adminUser;

    @Builder.Default
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}
