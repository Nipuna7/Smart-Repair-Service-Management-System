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

    // BUSINESS LOGIC 5: Set initial status to REQUESTED and set created timestamp
    // This method initializes the repair status to REQUESTED when created
    public void setInitialRepairStatus(Repair repair) {
        // Set initial status to REQUESTED
        repair.setStatus(Repair.RepairStatus.REQUESTED);

        // Set payment status to PENDING
        repair.setPaymentStatus(Repair.PaymentStatus.PENDING);

        // Created timestamp is automatically set by @CreationTimestamp annotation
    }

    // BUSINESS LOGIC 6: Assign repair priority based on service type
    // This method automatically determines priority based on service type
    public Repair.RepairPriority assignPriorityByServiceType(Repair.ServiceType serviceType) {
        // Assign priority based on service type
        switch (serviceType) {
            case BREAKDOWN:
                // Breakdowns are always URGENT - vehicle is not operational
                return Repair.RepairPriority.URGENT;

            case ENGINE_REPAIR:
            case ELECTRICAL:
                // Critical repairs are HIGH priority - safety and functionality issues
                return Repair.RepairPriority.HIGH;

            case BODY_REPAIR:
            case TIRE_SERVICE:
            case INSPECTION:
                // Less critical repairs are NORMAL priority
                return Repair.RepairPriority.NORMAL;

            case REGULAR_SERVICE:
            case OTHER:
            default:
                // Routine maintenance is LOW priority
                return Repair.RepairPriority.LOW;
        }
    }

    // BUSINESS LOGIC 7: Create repair request for customer's vehicle
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

        // BUSINESS LOGIC 5: Set initial status to REQUESTED and set created timestamp
        setInitialRepairStatus(repair);

        // Auto-generate repair request number
        repair.setRepairRequestNumber(generateRepairRequestNumber());

        // BUSINESS LOGIC 6: Assign repair priority based on service type
        // If priority is provided by customer, use it; otherwise, auto-assign based on service type
        if (requestDto.getPriority() != null) {
            repair.setPriority(requestDto.getPriority());
        } else {
            // Auto-assign priority based on service type
            repair.setPriority(assignPriorityByServiceType(requestDto.getServiceType()));
        }

        // Save repair to database
        Repair savedRepair = repairRepository.save(repair);

        // Convert to DTO and return
        return convertToResponseDto(savedRepair);
    }

    // BUSINESS LOGIC 8: Allow customer to view ONLY their own repair requests
    // This method retrieves all repair requests for a specific customer
    @Transactional(readOnly = true)
    public List<RepairResponseDto> getCustomerRepairRequests(Long customerId) {
        // Verify customer exists
        userRepository.findById(customerId)
                .orElseThrow(() -> new RuntimeException("Customer not found with id: " + customerId));

        // Find all repair requests for this customer
        List<Repair> repairs = repairRepository.findByCustomerId(customerId);

        // Convert to DTOs and return
        return repairs.stream()
                .map(this::convertToResponseDto)
                .toList();
    }

    // BUSINESS LOGIC 9: Handle cost estimate approval/rejection
    // This method allows customer to approve or reject cost estimate
    @Transactional
    public RepairResponseDto handleCostEstimateApproval(Long repairId, Long customerId, boolean approved) {
        // Find the repair by ID
        Repair repair = repairRepository.findById(repairId)
                .orElseThrow(() -> new RuntimeException("Repair not found with id: " + repairId));

        // Verify the repair belongs to the customer
        if (!repair.getCustomer().getId().equals(customerId)) {
            throw new RuntimeException("Access denied. You can only approve/reject your own repair estimates");
        }

        // Verify repair is in ESTIMATE_SUBMITTED status
        if (repair.getStatus() != Repair.RepairStatus.ESTIMATE_SUBMITTED) {
            throw new RuntimeException("Cannot approve/reject estimate. Repair status must be ESTIMATE_SUBMITTED");
        }

        // Verify estimated cost is set
        if (repair.getEstimatedCost() == null) {
            throw new RuntimeException("No cost estimate has been submitted for this repair");
        }

        // Handle approval or rejection
        if (approved) {
            // Customer approved the estimate
            repair.setEstimateApproved(true);
            repair.setStatus(Repair.RepairStatus.APPROVED);
        } else {
            // Customer rejected the estimate
            repair.setEstimateApproved(false);
            repair.setStatus(Repair.RepairStatus.CANCELLED);
            repair.setCancellationReason("Customer rejected the cost estimate");
            repair.setCancelledAt(java.time.LocalDateTime.now());
        }

        // Save updated repair
        Repair updatedRepair = repairRepository.save(repair);

        // Convert to DTO and return
        return convertToResponseDto(updatedRepair);
    }

    // BUSINESS LOGIC 10: Allow cancellation ONLY when status = REQUESTED or ASSIGNED
    // This method allows customer to cancel a repair request only in early stages
    @Transactional
    public RepairResponseDto cancelRepairRequest(Long repairId, Long customerId, String cancellationReason) {
        // Find the repair by ID
        Repair repair = repairRepository.findById(repairId)
                .orElseThrow(() -> new RuntimeException("Repair not found with id: " + repairId));

        // Verify the repair belongs to the customer
        if (!repair.getCustomer().getId().equals(customerId)) {
            throw new RuntimeException("Access denied. You can only cancel your own repair requests");
        }

        // Check if repair can be cancelled (only REQUESTED or ASSIGNED status allowed)
        if (repair.getStatus() != Repair.RepairStatus.REQUESTED &&
            repair.getStatus() != Repair.RepairStatus.ASSIGNED) {
            throw new RuntimeException(
                "Cannot cancel repair. Cancellation is only allowed for REQUESTED or ASSIGNED status. " +
                "Current status: " + repair.getStatus()
            );
        }

        // Validate cancellation reason
        if (cancellationReason == null || cancellationReason.trim().isEmpty()) {
            throw new IllegalArgumentException("Cancellation reason is required");
        }

        // Update repair to cancelled status
        repair.setStatus(Repair.RepairStatus.CANCELLED);
        repair.setCancellationReason(cancellationReason.trim());
        repair.setCancelledAt(java.time.LocalDateTime.now());

        // Save updated repair
        Repair updatedRepair = repairRepository.save(repair);

        // Convert to DTO and return
        return convertToResponseDto(updatedRepair);
    }

    // BUSINESS LOGIC 11: Lock repair data once status = COMPLETED
    // This method validates that completed repairs cannot be modified
    public void validateRepairNotCompleted(Repair repair) {
        // Check if repair is completed or delivered (locked statuses)
        if (repair.getStatus() == Repair.RepairStatus.COMPLETED ||
            repair.getStatus() == Repair.RepairStatus.DELIVERED) {
            throw new RuntimeException(
                "Cannot modify repair. This repair is locked because it has been completed. " +
                "Status: " + repair.getStatus() + ", Completed at: " + repair.getCompletedAt()
            );
        }
    }

    // BUSINESS LOGIC 12A: Expose method for fetching repair history per customer
    // This method retrieves all repair requests (including completed/cancelled) for a customer
    @Transactional(readOnly = true)
    public List<RepairResponseDto> getRepairHistoryByCustomer(Long customerId) {
        // Verify customer exists
        userRepository.findById(customerId)
                .orElseThrow(() -> new RuntimeException("Customer not found with id: " + customerId));

        // Find all repair requests for this customer (all statuses - complete history)
        List<Repair> repairs = repairRepository.findByCustomerId(customerId);

        // Sort by creation date (newest first) and convert to DTOs
        return repairs.stream()
                .sorted((r1, r2) -> r2.getCreatedAt().compareTo(r1.getCreatedAt()))
                .map(this::convertToResponseDto)
                .toList();
    }

    // BUSINESS LOGIC 12B: Expose method for fetching repair history per vehicle
    // This method retrieves all repair requests for a specific vehicle
    @Transactional(readOnly = true)
    public List<RepairResponseDto> getRepairHistoryByVehicle(Long vehicleId, Long customerId) {
        // Find the vehicle by ID
        Vehicle vehicle = vehicleRepository.findById(vehicleId)
                .orElseThrow(() -> new RuntimeException("Vehicle not found with id: " + vehicleId));

        // Verify the vehicle belongs to the customer
        if (!vehicle.getCustomer().getId().equals(customerId)) {
            throw new RuntimeException("Access denied. You can only view repair history for your own vehicles");
        }

        // Find all repair requests for this vehicle (all statuses - complete history)
        List<Repair> repairs = repairRepository.findByVehicleId(vehicleId);

        // Sort by creation date (newest first) and convert to DTOs
        return repairs.stream()
                .sorted((r1, r2) -> r2.getCreatedAt().compareTo(r1.getCreatedAt()))
                .map(this::convertToResponseDto)
                .toList();
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

