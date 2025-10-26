package com.ayd.parkcontrol.infrastructure.persistence.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "business_free_hours")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BusinessFreeHoursEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "ticket_id", nullable = false)
    private Long ticketId;

    @Column(name = "business_id", nullable = false)
    private Long businessId;

    @Column(name = "branch_id", nullable = false)
    private Long branchId;

    @Column(name = "granted_hours", nullable = false, precision = 10, scale = 2)
    private BigDecimal grantedHours;

    @Column(name = "granted_at", nullable = false)
    private LocalDateTime grantedAt;

    @Column(name = "is_settled", nullable = false)
    @Builder.Default
    private Boolean isSettled = false;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        if (grantedAt == null) {
            grantedAt = LocalDateTime.now();
        }
    }
}
