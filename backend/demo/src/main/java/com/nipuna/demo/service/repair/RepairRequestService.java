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

// Service class for managing repair requests with two separate business logics
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

    // BUSINESS LOGIC 2: Create repair request for customer's vehicle
    // This method creates a new repair request after validation
    @Transactional
    public RepairResponseDto createRepairRequest(RepairRequestDto requestDto, Long customerId) {
        // First validate vehicle ownership
        validateVehicleOwnership(requestDto.getVehicleId(), customerId);

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
                .vehicleId(repair.getVehicle().getId())
                .vehicleNumber(repair.getVehicle().getVehicleNumber())
                .issueDescription(repair.getIssueDescription())
                .serviceType(repair.getServiceType())
                .status(repair.getStatus())
                .priority(repair.getPriority())
                .estimatedCost(repair.getEstimatedCost())
                .finalCost(repair.getFinalCost())
                .createdAt(repair.getCreatedAt())
                .updatedAt(repair.getUpdatedAt())
                .build();
    }
}

