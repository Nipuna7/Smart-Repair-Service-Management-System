package com.nipuna.demo.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "repairs")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Repair {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "repair_request_number", unique = true)
    private String repairRequestNumber;


    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "vehicle_id", nullable = false)
    private Vehicle vehicle;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id", nullable = false)
    private User customer;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "technician_id")
    private User technician;

    @Enumerated(EnumType.STRING)
    @Column(name = "service_type", nullable = false)
    private ServiceType serviceType;

    @Column(name = "issue_description", nullable = false, columnDefinition = "TEXT")
    private String issueDescription;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private RepairStatus status = RepairStatus.REQUESTED;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private RepairPriority priority = RepairPriority.NORMAL;

    @Column(name = "estimated_cost", precision = 10, scale = 2)
    private BigDecimal estimatedCost;

    @Column(name = "final_cost", precision = 10, scale = 2)
    private BigDecimal finalCost;

    @Enumerated(EnumType.STRING)
    @Column(name = "payment_status")
    private PaymentStatus paymentStatus = PaymentStatus.PENDING;

    @Column(name = "estimate_approved")
    private Boolean estimateApproved;

    @Column(name = "assigned_at")
    private LocalDateTime assignedAt;

    @Column(name = "in_progress_at")
    private LocalDateTime inProgressAt;

    @Column(name = "completed_at")
    private LocalDateTime completedAt;

    @Column(name = "cancelled_at")
    private LocalDateTime cancelledAt;

    @Column(name = "cancellation_reason", columnDefinition = "TEXT")
    private String cancellationReason;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    public enum ServiceType {
        BREAKDOWN,
        REGULAR_SERVICE,
        INSPECTION,
        BODY_REPAIR,
        ENGINE_REPAIR,
        ELECTRICAL,
        TIRE_SERVICE,
        OTHER
    }

    public enum RepairStatus {
        REQUESTED,
        ASSIGNED,
        IN_PROGRESS,
        ESTIMATE_SUBMITTED,
        APPROVED,
        COMPLETED,
        CANCELLED,
        DELIVERED
    }

    public enum RepairPriority {
        URGENT,
        HIGH,
        NORMAL,
        LOW
    }

    public enum PaymentStatus {
        PENDING,
        PAID,
        REFUNDED
    }
}
