package com.ayd.parkcontrol.infrastructure.persistence.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "ticket_charges")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TicketChargeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "ticket_id", nullable = false)
    private Long ticketId;

    @Column(name = "total_hours", nullable = false, precision = 10, scale = 2)
    private BigDecimal totalHours;

    @Column(name = "free_hours_granted", nullable = false, precision = 10, scale = 2)
    @Builder.Default
    private BigDecimal freeHoursGranted = BigDecimal.ZERO;

    @Column(name = "billable_hours", nullable = false, precision = 10, scale = 2)
    private BigDecimal billableHours;

    @Column(name = "rate_applied", nullable = false, precision = 10, scale = 2)
    private BigDecimal rateApplied;

    @Column(name = "subtotal", nullable = false, precision = 10, scale = 2)
    private BigDecimal subtotal;

    @Column(name = "subscription_hours_consumed", precision = 10, scale = 2)
    @Builder.Default
    private BigDecimal subscriptionHoursConsumed = BigDecimal.ZERO;

    @Column(name = "subscription_overage_hours", precision = 10, scale = 2)
    @Builder.Default
    private BigDecimal subscriptionOverageHours = BigDecimal.ZERO;

    @Column(name = "subscription_overage_charge", precision = 10, scale = 2)
    @Builder.Default
    private BigDecimal subscriptionOverageCharge = BigDecimal.ZERO;

    @Column(name = "total_amount", nullable = false, precision = 10, scale = 2)
    private BigDecimal totalAmount;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}
