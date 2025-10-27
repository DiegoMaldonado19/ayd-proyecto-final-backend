package com.ayd.parkcontrol.infrastructure.persistence.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "temporal_permits")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TemporalPermitEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "subscription_id", nullable = false)
    private Long subscriptionId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "subscription_id", insertable = false, updatable = false)
    private SubscriptionEntity subscription;

    @Column(name = "temporal_plate", nullable = false, length = 20)
    private String temporalPlate;

    @Column(name = "start_date", nullable = false)
    private LocalDateTime startDate;

    @Column(name = "end_date", nullable = false)
    private LocalDateTime endDate;

    @Column(name = "max_uses", nullable = false)
    private Integer maxUses;

    @Builder.Default
    @Column(name = "current_uses", nullable = false)
    private Integer currentUses = 0;

    @Column(name = "allowed_branches", columnDefinition = "JSON")
    private String allowedBranches;

    @Column(name = "vehicle_type_id", nullable = false)
    private Integer vehicleTypeId;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "vehicle_type_id", insertable = false, updatable = false)
    private VehicleTypeEntity vehicleType;

    @Column(name = "status_type_id", nullable = false)
    private Integer statusTypeId;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "status_type_id", insertable = false, updatable = false)
    private TemporalPermitStatusTypeEntity status;

    @Column(name = "approved_by", nullable = false)
    private Long approvedBy;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "approved_by", insertable = false, updatable = false)
    private UserEntity approver;

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
