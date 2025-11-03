package com.ayd.parkcontrol.infrastructure.persistence.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "subscription_overages")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SubscriptionOverageEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "subscription_id", nullable = false)
    private Long subscriptionId;

    @Column(name = "ticket_id", nullable = false)
    private Long ticketId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ticket_id", insertable = false, updatable = false)
    private TicketEntity ticket;

    @Column(name = "overage_hours", nullable = false, precision = 10, scale = 2)
    private BigDecimal overageHours;

    @Column(name = "charged_amount", nullable = false, precision = 10, scale = 2)
    private BigDecimal chargedAmount;

    @Column(name = "applied_rate", nullable = false, precision = 10, scale = 2)
    private BigDecimal appliedRate;

    @Column(name = "charged_at", nullable = false)
    private LocalDateTime chargedAt;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}
