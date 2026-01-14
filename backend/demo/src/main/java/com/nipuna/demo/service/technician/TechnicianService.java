package com.nipuna.demo.service.technician;

import com.nipuna.demo.dto.repair.RepairResponseDto;
import com.nipuna.demo.dto.vehical.VehicleResponseDto;
import com.nipuna.demo.entity.Repair;
import com.nipuna.demo.entity.User;
import com.nipuna.demo.entity.Vehicle;
import com.nipuna.demo.repository.RepairRepository;
import com.nipuna.demo.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TechnicianService {

    private final RepairRepository repairRepository;
    private final UserRepository userRepository;

    // ===== ASSIGNED REPAIR MANAGEMENT SERVICE =====

    // Get authenticated technician
    private User getAuthenticatedTechnician(Authentication authentication) {
        String username = authentication.getName();
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("Technician not found: " + username));
    }

    // 1. View list of assigned repair requests
    @Transactional(readOnly = true)
    public List<RepairResponseDto> getAssignedRepairs(Authentication authentication) {
        User technician = getAuthenticatedTechnician(authentication);

        // Find all repairs assigned to this technician
        List<Repair> assignedRepairs = repairRepository.findByTechnicianId(technician.getId());

        // Convert to DTOs and return
        return assignedRepairs.stream()
                .map(this::mapToRepairResponseDto)
                .collect(Collectors.toList());
    }

    // 2. View repair details (vehicle, issue, damage photos)
    @Transactional(readOnly = true)
    public Map<String, Object> getAssignedRepairDetails(Long repairId, Authentication authentication) {
        User technician = getAuthenticatedTechnician(authentication);

        // Find the repair by ID
        Repair repair = repairRepository.findById(repairId)
                .orElseThrow(() -> new RuntimeException("Repair not found with id: " + repairId));

        // Verify the repair is assigned to this technician
        if (repair.getTechnician() == null || !repair.getTechnician().getId().equals(technician.getId())) {
            throw new RuntimeException("Access denied. This repair is not assigned to you");
        }

        // Build detailed repair information
        Map<String, Object> repairDetails = new HashMap<>();

        // Repair basic info
        repairDetails.put("repairId", repair.getId());
        repairDetails.put("repairRequestNumber", repair.getRepairRequestNumber());
        repairDetails.put("status", repair.getStatus());
        repairDetails.put("priority", repair.getPriority());
        repairDetails.put("serviceType", repair.getServiceType());
        repairDetails.put("issueDescription", repair.getIssueDescription());

        // Vehicle information
        Vehicle vehicle = repair.getVehicle();
        Map<String, Object> vehicleInfo = new HashMap<>();
        vehicleInfo.put("vehicleId", vehicle.getId());
        vehicleInfo.put("vehicleNumber", vehicle.getVehicleNumber());
        vehicleInfo.put("make", vehicle.getMake());
        vehicleInfo.put("model", vehicle.getModel());
        vehicleInfo.put("year", vehicle.getYear());
        vehicleInfo.put("vehicleType", vehicle.getVehicleType());
        repairDetails.put("vehicle", vehicleInfo);

        // Customer information
        User customer = repair.getCustomer();
        Map<String, Object> customerInfo = new HashMap<>();
        customerInfo.put("customerId", customer.getId());
        customerInfo.put("customerName", customer.getFullName());
        customerInfo.put("customerEmail", customer.getEmail());
        customerInfo.put("customerPhone", customer.getPhoneNumber());
        repairDetails.put("customer", customerInfo);

        // Cost information
        repairDetails.put("estimatedCost", repair.getEstimatedCost());
        repairDetails.put("finalCost", repair.getFinalCost());
        repairDetails.put("estimateApproved", repair.getEstimateApproved());
        repairDetails.put("paymentStatus", repair.getPaymentStatus());

        // Timeline information
        repairDetails.put("createdAt", repair.getCreatedAt());
        repairDetails.put("assignedAt", repair.getAssignedAt());
        repairDetails.put("inProgressAt", repair.getInProgressAt());
        repairDetails.put("completedAt", repair.getCompletedAt());

        // Note: Damage photos would be added here when file upload is implemented
        repairDetails.put("damagePhotos", "Feature to be implemented");

        return repairDetails;
    }

    // 3. Access repair history for the vehicle
    @Transactional(readOnly = true)
    public List<RepairResponseDto> getVehicleRepairHistory(Long repairId, Authentication authentication) {
        User technician = getAuthenticatedTechnician(authentication);

        // Find the current repair
        Repair currentRepair = repairRepository.findById(repairId)
                .orElseThrow(() -> new RuntimeException("Repair not found with id: " + repairId));

        // Verify the repair is assigned to this technician
        if (currentRepair.getTechnician() == null ||
            !currentRepair.getTechnician().getId().equals(technician.getId())) {
            throw new RuntimeException("Access denied. This repair is not assigned to you");
        }

        // Get vehicle ID from current repair
        Long vehicleId = currentRepair.getVehicle().getId();

        // Find all repairs for this vehicle
        List<Repair> vehicleRepairs = repairRepository.findByVehicleId(vehicleId);

        // Sort by creation date (newest first) and convert to DTOs
        return vehicleRepairs.stream()
                .sorted((r1, r2) -> r2.getCreatedAt().compareTo(r1.getCreatedAt()))
                .map(this::mapToRepairResponseDto)
                .collect(Collectors.toList());
    }

    // Get vehicle information for assigned repair
    @Transactional(readOnly = true)
    public VehicleResponseDto getVehicleInfo(Long repairId, Authentication authentication) {
        User technician = getAuthenticatedTechnician(authentication);

        // Find the repair
        Repair repair = repairRepository.findById(repairId)
                .orElseThrow(() -> new RuntimeException("Repair not found with id: " + repairId));

        // Verify the repair is assigned to this technician
        if (repair.getTechnician() == null ||
            !repair.getTechnician().getId().equals(technician.getId())) {
            throw new RuntimeException("Access denied. This repair is not assigned to you");
        }

        // Get vehicle and convert to DTO
        Vehicle vehicle = repair.getVehicle();
        return mapToVehicleResponseDto(vehicle);
    }

    // Get customer contact information for assigned repair
    @Transactional(readOnly = true)
    public Map<String, Object> getCustomerContact(Long repairId, Authentication authentication) {
        User technician = getAuthenticatedTechnician(authentication);

        // Find the repair
        Repair repair = repairRepository.findById(repairId)
                .orElseThrow(() -> new RuntimeException("Repair not found with id: " + repairId));

        // Verify the repair is assigned to this technician
        if (repair.getTechnician() == null ||
            !repair.getTechnician().getId().equals(technician.getId())) {
            throw new RuntimeException("Access denied. This repair is not assigned to you");
        }

        // Build customer contact info
        User customer = repair.getCustomer();
        Map<String, Object> contactInfo = new HashMap<>();
        contactInfo.put("customerId", customer.getId());
        contactInfo.put("customerName", customer.getFullName());
        contactInfo.put("email", customer.getEmail());
        contactInfo.put("phone", customer.getPhoneNumber());
        contactInfo.put("address", customer.getAddress());

        return contactInfo;
    }

    // ===== HELPER METHODS =====

    // Convert Repair entity to RepairResponseDto
    private RepairResponseDto mapToRepairResponseDto(Repair repair) {
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
                // Technician information
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

    // Convert Vehicle entity to VehicleResponseDto
    private VehicleResponseDto mapToVehicleResponseDto(Vehicle vehicle) {
        return VehicleResponseDto.builder()
                .id(vehicle.getId())
                .vehicleNumber(vehicle.getVehicleNumber())
                .make(vehicle.getMake())
                .model(vehicle.getModel())
                .year(vehicle.getYear())
                .vehicleType(vehicle.getVehicleType())
                .customerId(vehicle.getCustomer().getId())
                .customerName(vehicle.getCustomer().getFullName())
                .createdAt(vehicle.getCreatedAt())
                .updatedAt(vehicle.getUpdatedAt())
                .build();
    }
}
