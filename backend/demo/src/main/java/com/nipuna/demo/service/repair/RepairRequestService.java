package com.nipuna.demo.service.repair;

import com.nipuna.demo.dto.repair.RepairRequestDto;
import com.nipuna.demo.dto.repair.RepairResponseDto;
import com.nipuna.demo.entity.Repair;
import com.nipuna.demo.entity.User;
import com.nipuna.demo.entity.Vehicle;
import com.nipuna.demo.repository.RepairRepository;
import com.nipuna.demo.repository.UserRepository;
import com.nipuna.demo.repository.VehicleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;

// Service class for managing repair requests with business logic
@Service
@RequiredArgsConstructor
public class RepairRequestService {

    private final RepairRepository repairRepository;
    private final VehicleRepository vehicleRepository;
    private final UserRepository userRepository;

    // BUSINESS LOGIC 1: Validate vehicle ownership
    // This method checks if the vehicle belongs to the customer
    @Transactional(readOnly = true)
    public boolean validateVehicleOwnership(Long vehicleId, Long customerId) {
        // Find the vehicle by ID
        Vehicle vehicle = vehicleRepository.findById(vehicleId)
                .orElseThrow(() -> new RuntimeException("Vehicle not found with id: " + vehicleId));

        // Check if vehicle belongs to the customer
        if (!vehicle.getCustomer().getId().equals(customerId)) {
            throw new RuntimeException("Access denied. You can only access your own vehicles");
        }

        // Return true if validation passes
        return true;
    }

    // BUSINESS LOGIC 2: Prevent multiple active repair requests for the same vehicle
    // This method checks if the vehicle already has an active repair request
    @Transactional(readOnly = true)
    public void preventMultipleActiveRepairRequests(Long vehicleId) {
        // Define active repair statuses
        List<Repair.RepairStatus> activeStatuses = Arrays.asList(
                Repair.RepairStatus.REQUESTED,
                Repair.RepairStatus.ASSIGNED,
                Repair.RepairStatus.IN_PROGRESS,
                Repair.RepairStatus.ESTIMATE_SUBMITTED,
                Repair.RepairStatus.APPROVED
        );

        // Check if vehicle has any active repair
        boolean hasActiveRepair = repairRepository.existsByVehicleIdAndStatusIn(vehicleId, activeStatuses);

        // Throw exception if active repair exists
        if (hasActiveRepair) {
            throw new RuntimeException("This vehicle already has an active repair request. Please wait until the current repair is completed.");
        }
    }

    // BUSINESS LOGIC 3: Validate input (issue description, service type)
    // This method validates the repair request input data
    @Transactional(readOnly = true)
    public void validateRepairRequestInput(RepairRequestDto requestDto) {
        if (requestDto == null) {
            throw new IllegalArgumentException("Request cannot be null");
        }
        if (requestDto.getServiceType() == null) {
            throw new IllegalArgumentException("Service type is required");
        }
        if (requestDto.getIssueDescription() == null || requestDto.getIssueDescription().trim().isEmpty()) {
            throw new IllegalArgumentException("Issue description is required and must not be empty");
        }
        if (requestDto.getIssueDescription().trim().length() < 10) {
            throw new IllegalArgumentException("Issue description must be at least 10 characters long");
        }
        if (requestDto.getIssueDescription().trim().length() > 1000) {
            throw new IllegalArgumentException("Issue description must not exceed 1000 characters");
        }
    }

    // BUSINESS LOGIC 4: Auto-generate repair request number
    // This method generates a unique repair request number (format: RR-YYYYMMDD-XXXX)
    @Transactional
    public String generateRepairRequestNumber() {
        String datePart = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        String prefix = "RR-" + datePart + "-";

        // Get the count of all repair requests (use total count + 1 for sequence)
        long totalRepairs = repairRepository.count();

        // Generate sequence number with 4 digits (padded with zeros)
        String sequenceNumber = String.format("%04d", totalRepairs + 1);

        // Return complete repair request number
        return prefix + sequenceNumber;
    }

    // BUSINESS LOGIC 5: Create repair request for customer's vehicle
    // This method creates a new repair request after validation
    @Transactional
    public RepairResponseDto createRepairRequest(RepairRequestDto requestDto, Long customerId) {
        // First validate vehicle ownership
        validateVehicleOwnership(requestDto.getVehicleId(), customerId);

        // Validate input (issue description, service type)
        validateRepairRequestInput(requestDto);

        // Prevent multiple active repair requests for the same vehicle
        preventMultipleActiveRepairRequests(requestDto.getVehicleId());

        // Find the vehicle by ID
        Vehicle vehicle = vehicleRepository.findById(requestDto.getVehicleId())
                .orElseThrow(() -> new RuntimeException("Vehicle not found with id: " + requestDto.getVehicleId()));

        // Find the customer
        User customer = userRepository.findById(customerId)
                .orElseThrow(() -> new RuntimeException("Customer not found with id: " + customerId));

        // Create new repair entity
        Repair repair = new Repair();
        repair.setVehicle(vehicle);
        repair.setCustomer(customer);
        repair.setServiceType(requestDto.getServiceType());
        repair.setIssueDescription(requestDto.getIssueDescription());
        repair.setStatus(Repair.RepairStatus.REQUESTED);

        // Step 4: Auto-generate repair request number
        repair.setRepairRequestNumber(generateRepairRequestNumber());

        // Set priority - use provided or default to NORMAL
        if (requestDto.getPriority() != null) {
            repair.setPriority(requestDto.getPriority());
        } else {
            repair.setPriority(Repair.RepairPriority.NORMAL);
        }

        repair.setPaymentStatus(Repair.PaymentStatus.PENDING);

        // Save repair to database
        Repair savedRepair = repairRepository.save(repair);

        // Convert to DTO and return
        return convertToResponseDto(savedRepair);
    }

    // Helper method to convert Repair entity to RepairResponseDto
    private RepairResponseDto convertToResponseDto(Repair repair) {
        return RepairResponseDto.builder()
                .id(repair.getId())
                .repairRequestNumber(repair.getRepairRequestNumber())
                // Vehicle information
                .vehicleId(repair.getVehicle().getId())
                .vehicleNumber(repair.getVehicle().getVehicleNumber())
                .vehicleMake(repair.getVehicle().getMake())
                .vehicleModel(repair.getVehicle().getModel())
                // Customer information
                .customerId(repair.getCustomer().getId())
                .customerName(repair.getCustomer().getFullName())
                .customerEmail(repair.getCustomer().getEmail())
                // Technician information (if assigned)
                .technicianId(repair.getTechnician() != null ? repair.getTechnician().getId() : null)
                .technicianName(repair.getTechnician() != null ? repair.getTechnician().getFullName() : null)
                // Repair details
                .serviceType(repair.getServiceType())
                .issueDescription(repair.getIssueDescription())
                .status(repair.getStatus())
                .priority(repair.getPriority())
                // Cost information
                .estimatedCost(repair.getEstimatedCost())
                .finalCost(repair.getFinalCost())
                .paymentStatus(repair.getPaymentStatus())
                // Approval status
                .estimateApproved(repair.getEstimateApproved())
                // Timestamps
                .createdAt(repair.getCreatedAt())
                .assignedAt(repair.getAssignedAt())
                .inProgressAt(repair.getInProgressAt())
                .completedAt(repair.getCompletedAt())
                .cancelledAt(repair.getCancelledAt())
                .updatedAt(repair.getUpdatedAt())
                // Additional information
                .cancellationReason(repair.getCancellationReason())
                .build();
    }
}

