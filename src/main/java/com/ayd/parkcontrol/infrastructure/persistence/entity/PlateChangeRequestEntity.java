package com.ayd.parkcontrol.infrastructure.persistence.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "plate_change_requests")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PlateChangeRequestEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "subscription_id", nullable = false)
    private Long subscriptionId;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "old_license_plate", nullable = false, length = 20)
    private String oldLicensePlate;

    @Column(name = "new_license_plate", nullable = false, length = 20)
    private String newLicensePlate;

    @Column(name = "reason_id", nullable = false)
    private Integer reasonId;

    @Column(length = 500)
    private String notes;

    @Column(name = "status_id", nullable = false)
    private Integer statusId;

    @Column(name = "reviewed_by")
    private Long reviewedBy;

    @Column(name = "reviewed_at")
    private LocalDateTime reviewedAt;

    @Column(name = "review_notes", length = 500)
    private String reviewNotes;

    @Column(name = "has_administrative_charge", nullable = false)
    private Boolean hasAdministrativeCharge;

    @Column(name = "administrative_charge_amount", precision = 10, scale = 2)
    private BigDecimal administrativeChargeAmount;

    @Column(name = "administrative_charge_reason", length = 500)
    private String administrativeChargeReason;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        if (hasAdministrativeCharge == null) {
            hasAdministrativeCharge = false;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
