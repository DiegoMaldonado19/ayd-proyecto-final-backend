package com.ayd.parkcontrol.infrastructure.persistence.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "subscription_plans")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SubscriptionPlanEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "plan_type_id", nullable = false)
    private Integer planTypeId;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "plan_type_id", insertable = false, updatable = false)
    private SubscriptionPlanTypeEntity planType;

    @Column(name = "monthly_hours", nullable = false)
    private Integer monthlyHours;

    @Column(name = "monthly_discount_percentage", nullable = false, precision = 5, scale = 2)
    private BigDecimal monthlyDiscountPercentage;

    @Column(name = "annual_additional_discount_percentage", nullable = false, precision = 5, scale = 2)
    private BigDecimal annualAdditionalDiscountPercentage;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Builder.Default
    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
