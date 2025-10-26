package com.ayd.parkcontrol.infrastructure.persistence.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "subscriptions")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SubscriptionEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", insertable = false, updatable = false)
    private UserEntity user;

    @Column(name = "plan_id", nullable = false)
    private Long planId;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "plan_id", insertable = false, updatable = false)
    private SubscriptionPlanEntity plan;

    @Column(name = "license_plate", nullable = false, length = 20)
    private String licensePlate;

    @Column(name = "frozen_rate_base", nullable = false, precision = 10, scale = 2)
    private BigDecimal frozenRateBase;

    @Column(name = "purchase_date", nullable = false)
    private LocalDateTime purchaseDate;

    @Column(name = "start_date", nullable = false)
    private LocalDateTime startDate;

    @Column(name = "end_date", nullable = false)
    private LocalDateTime endDate;

    @Builder.Default
    @Column(name = "consumed_hours", precision = 10, scale = 2)
    private BigDecimal consumedHours = BigDecimal.ZERO;

    @Column(name = "status_type_id", nullable = false)
    private Integer statusTypeId;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "status_type_id", insertable = false, updatable = false)
    private SubscriptionStatusTypeEntity status;

    @Builder.Default
    @Column(name = "is_annual", nullable = false)
    private Boolean isAnnual = false;

    @Builder.Default
    @Column(name = "notified_80_percent", nullable = false)
    private Boolean notified80Percent = false;

    @Builder.Default
    @Column(name = "auto_renew_enabled", nullable = false)
    private Boolean autoRenewEnabled = false;

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
