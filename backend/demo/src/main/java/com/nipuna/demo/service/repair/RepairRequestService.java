package com.nipuna.demo.service.repair;

import com.nipuna.demo.dto.repair.RepairRequestDto;
import com.nipuna.demo.dto.RepairResponseDto;
import com.nipuna.demo.entity.Repair;
import com.nipuna.demo.entity.User;
import com.nipuna.demo.entity.Vehicle;
import com.nipuna.demo.enums.RepairPriority;
import com.nipuna.demo.enums.RepairStatus;
import com.nipuna.demo.enums.ServiceType;
import com.nipuna.demo.repository.RepairRepository;
import com.nipuna.demo.repository.UserRepository;
import com.nipuna.demo.repository.VehicleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Transactional
public class RepairRequestService {

    private final RepairRepository repairRepository;
    private final VehicleRepository vehicleRepository;
    private final UserRepository userRepository;

    /**
     * BUSINESS LOGIC 1: Create a repair request for a vehicle owned by the logged-in customer
     *
     * Rules:
     * - Vehicle must exist
     * - Vehicle must belong to the logged-in customer (BUSINESS LOGIC 2)
     * - Issue description cannot be empty
     * - Service type must be provided
     * - Prevent multiple active repairs for the same vehicle
     * - Auto-set initial repair status to REQUESTED
     * - Auto-assign repair priority based on service type (breakdown > regular service)
     */
    public RepairResponseDto createRepairRequest(RepairRequestDto requestDto) {
        // Get logged-in customer
        User customer = getLoggedInCustomer();

        // Validate that vehicle exists
        Vehicle vehicle = vehicleRepository.findById(requestDto.getVehicleId())
                .orElseThrow(() -> new RuntimeException("Vehicle not found"));

        // BUSINESS LOGIC 2: Validate that the vehicle belongs to the customer
        validateVehicleOwnership(vehicle, customer);

        // Validate input
        validateRepairRequestInput(requestDto);

        // Check for active repairs on the same vehicle
        boolean hasActiveRepair = repairRepository.existsByVehicleAndStatusIn(
                vehicle,
                java.util.Arrays.asList(
                        RepairStatus.REQUESTED,
                        RepairStatus.ASSIGNED,
                        RepairStatus.IN_PROGRESS,
                        RepairStatus.ESTIMATE_SUBMITTED,
                        RepairStatus.APPROVED
                )
        );

        if (hasActiveRepair) {
            throw new RuntimeException("This vehicle already has an active repair request. Please wait until it is completed.");
        }

        // Create repair entity
        Repair repair = new Repair();
        repair.setVehicle(vehicle);
        repair.setCustomer(customer);
        repair.setIssueDescription(requestDto.getIssueDescription());
        repair.setServiceType(requestDto.getServiceType());

        // Auto-set initial status to REQUESTED
        repair.setStatus(RepairStatus.REQUESTED);

        // Auto-assign priority based on service type
        repair.setPriority(determinePriority(requestDto.getServiceType()));

        repair.setRequestedDate(LocalDateTime.now());
        repair.setCreatedAt(LocalDateTime.now());
        repair.setUpdatedAt(LocalDateTime.now());

        // Save repair
        Repair savedRepair = repairRepository.save(repair);

        // Convert to DTO and return
        return convertToResponseDto(savedRepair);
    }

    /**
     * BUSINESS LOGIC 2: Validate that the vehicle belongs to the customer
     *
     * Security Rule: A customer can only create repair requests for their own vehicles
     */
    private void validateVehicleOwnership(Vehicle vehicle, User customer) {
        if (!vehicle.getOwner().getId().equals(customer.getId())) {
            throw new RuntimeException("Access denied. You can only create repair requests for your own vehicles.");
        }
    }

    /**
     * Validate repair request input
     */
    private void validateRepairRequestInput(RepairRequestDto requestDto) {
        if (requestDto.getIssueDescription() == null || requestDto.getIssueDescription().trim().isEmpty()) {
            throw new RuntimeException("Issue description cannot be empty");
        }

        if (requestDto.getServiceType() == null) {
            throw new RuntimeException("Service type must be provided");
        }

        if (requestDto.getVehicleId() == null) {
            throw new RuntimeException("Vehicle ID must be provided");
        }
    }

    /**
     * Auto-assign repair priority based on service type
     * Business Rule: Breakdown > Regular Service
     */
    private RepairPriority determinePriority(ServiceType serviceType) {
        switch (serviceType) {
            case BREAKDOWN:
            case EMERGENCY:
                return RepairPriority.HIGH;
            case MAJOR_SERVICE:
            case ENGINE_REPAIR:
            case TRANSMISSION_REPAIR:
                return RepairPriority.MEDIUM;
            case REGULAR_SERVICE:
            case MINOR_SERVICE:
            case INSPECTION:
            case TIRE_SERVICE:
            case BATTERY_SERVICE:
            case BRAKE_SERVICE:
            case OIL_CHANGE:
            case CLEANING:
                return RepairPriority.LOW;
            default:
                return RepairPriority.MEDIUM;
        }
    }

    /**
     * Get logged-in customer from security context
     */
    private User getLoggedInCustomer() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();

        return userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Customer not found"));
    }

    /**
     * Convert Repair entity to RepairResponseDto
     */
    private RepairResponseDto convertToResponseDto(Repair repair) {
        RepairResponseDto dto = new RepairResponseDto();
        dto.setId(repair.getId());
        dto.setVehicleId(repair.getVehicle().getId());
        dto.setVehicleNumber(repair.getVehicle().getVehicleNumber());
        dto.setIssueDescription(repair.getIssueDescription());
        dto.setServiceType(repair.getServiceType());
        dto.setStatus(repair.getStatus());
        dto.setPriority(repair.getPriority());
        dto.setRequestedDate(repair.getRequestedDate());
        dto.setEstimatedCompletionDate(repair.getEstimatedCompletionDate());
        dto.setActualCompletionDate(repair.getActualCompletionDate());
        dto.setEstimatedCost(repair.getEstimatedCost());
        dto.setActualCost(repair.getActualCost());
        dto.setCreatedAt(repair.getCreatedAt());
        dto.setUpdatedAt(repair.getUpdatedAt());
        return dto;
    }
}
