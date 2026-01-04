package com.nipuna.demo.dto.repair;

import com.nipuna.demo.entity.Repair;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;


// DTO for repair response
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RepairResponseDto {


    // Unique identifier for the repair request
    private Long id;

    //Vehicle information
    private Long vehicleId;
    private String vehicleNumber;
    private String vehicleMake;
    private String vehicleModel;

    //Customer information
    private Long customerId;
    private String customerName;
    private String customerEmail;

    // Technician information (if assigned)
    private Long technicianId;
    private String technicianName;

    // Repair details
    private Repair.ServiceType serviceType;
    private String issueDescription;
    private Repair.RepairStatus status;
    private Repair.RepairPriority priority;

    // Cost information
    private BigDecimal estimatedCost;
    private BigDecimal finalCost;
    private Repair.PaymentStatus paymentStatus;

    // Approval status
    private Boolean estimateApproved;

    // Timestamps
    private LocalDateTime createdAt;
    private LocalDateTime assignedAt;
    private LocalDateTime inProgressAt;
    private LocalDateTime completedAt;
    private LocalDateTime cancelledAt;
    private LocalDateTime updatedAt;

    // Additional information
    private String cancellationReason;
}

