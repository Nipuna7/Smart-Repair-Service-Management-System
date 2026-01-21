package com.nipuna.demo.service.admin;

import com.nipuna.demo.dto.repair.RepairResponseDto;
import com.nipuna.demo.entity.Repair;
import com.nipuna.demo.repository.RepairRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AdminService {

    private final RepairRepository repairRepository;

    // ========== REPAIR REQUEST REVIEW SERVICE ==========

    // View all repair requests
    @Transactional(readOnly = true)
    public List<RepairResponseDto> getAllRepairRequests() {
        List<Repair> repairs = repairRepository.findAll();
        return repairs.stream()
                .map(this::convertToRepairResponseDto)
                .collect(Collectors.toList());
    }

    // View repair requests filtered by status
    @Transactional(readOnly = true)
    public List<RepairResponseDto> getRepairRequestsByStatus(Repair.RepairStatus status) {
        List<Repair> repairs = repairRepository.findAll();
        return repairs.stream()
                .filter(repair -> repair.getStatus() == status)
                .map(this::convertToRepairResponseDto)
                .collect(Collectors.toList());
    }

    // View repair requests filtered by priority
    @Transactional(readOnly = true)
    public List<RepairResponseDto> getRepairRequestsByPriority(Repair.RepairPriority priority) {
        List<Repair> repairs = repairRepository.findAll();
        return repairs.stream()
                .filter(repair -> repair.getPriority() == priority)
                .map(this::convertToRepairResponseDto)
                .collect(Collectors.toList());
    }

    // View repair requests filtered by date range
    @Transactional(readOnly = true)
    public List<RepairResponseDto> getRepairRequestsByDateRange(LocalDate startDate, LocalDate endDate) {
        List<Repair> repairs = repairRepository.findAll();
        LocalDateTime startDateTime = startDate.atStartOfDay();
        LocalDateTime endDateTime = endDate.atTime(23, 59, 59);

        return repairs.stream()
                .filter(repair -> {
                    LocalDateTime createdAt = repair.getCreatedAt();
                    return createdAt != null &&
                           !createdAt.isBefore(startDateTime) &&
                           !createdAt.isAfter(endDateTime);
                })
                .map(this::convertToRepairResponseDto)
                .collect(Collectors.toList());
    }

    // View specific repair request details by ID
    @Transactional(readOnly = true)
    public RepairResponseDto getRepairRequestById(Long repairId) {
        Repair repair = repairRepository.findById(repairId)
                .orElseThrow(() -> new RuntimeException("Repair request not found with ID: " + repairId));
        return convertToRepairResponseDto(repair);
    }

    // View repair requests filtered by multiple criteria (status, priority, date)
    @Transactional(readOnly = true)
    public List<RepairResponseDto> getRepairRequestsWithFilters(
            Repair.RepairStatus status,
            Repair.RepairPriority priority,
            LocalDate startDate,
            LocalDate endDate) {

        List<Repair> repairs = repairRepository.findAll();

        return repairs.stream()
                .filter(repair -> {
                    // Filter by status if provided
                    if (status != null && repair.getStatus() != status) {
                        return false;
                    }

                    // Filter by priority if provided
                    if (priority != null && repair.getPriority() != priority) {
                        return false;
                    }

                    // Filter by date range if provided
                    if (startDate != null && endDate != null) {
                        LocalDateTime createdAt = repair.getCreatedAt();
                        LocalDateTime startDateTime = startDate.atStartOfDay();
                        LocalDateTime endDateTime = endDate.atTime(23, 59, 59);

                        if (createdAt == null ||
                            createdAt.isBefore(startDateTime) ||
                            createdAt.isAfter(endDateTime)) {
                            return false;
                        }
                    }

                    return true;
                })
                .map(this::convertToRepairResponseDto)
                .collect(Collectors.toList());
    }

    // ========== HELPER METHODS ==========

    // Convert Repair entity to RepairResponseDto
    private RepairResponseDto convertToRepairResponseDto(Repair repair) {
        return RepairResponseDto.builder()
                .id(repair.getId())
                .repairRequestNumber(repair.getRepairRequestNumber())
                .vehicleId(repair.getVehicle().getId())
                .vehicleNumber(repair.getVehicle().getVehicleNumber())
                .vehicleMake(repair.getVehicle().getMake())
                .vehicleModel(repair.getVehicle().getModel())
                .customerId(repair.getCustomer().getId())
                .customerName(repair.getCustomer().getFullName())
                .customerEmail(repair.getCustomer().getEmail())
                .technicianId(repair.getTechnician() != null ? repair.getTechnician().getId() : null)
                .technicianName(repair.getTechnician() != null ? repair.getTechnician().getFullName() : null)
                .serviceType(repair.getServiceType())
                .issueDescription(repair.getIssueDescription())
                .status(repair.getStatus())
                .priority(repair.getPriority())
                .estimatedCost(repair.getEstimatedCost())
                .finalCost(repair.getFinalCost())
                .paymentStatus(repair.getPaymentStatus())
                .estimateApproved(repair.getEstimateApproved())
                .createdAt(repair.getCreatedAt())
                .assignedAt(repair.getAssignedAt())
                .inProgressAt(repair.getInProgressAt())
                .completedAt(repair.getCompletedAt())
                .cancelledAt(repair.getCancelledAt())
                .updatedAt(repair.getUpdatedAt())
                .cancellationReason(repair.getCancellationReason())
                .diagnosisDetails(repair.getDiagnosisDetails())
                .repairNotes(repair.getRepairNotes())
                .build();
    }
}
