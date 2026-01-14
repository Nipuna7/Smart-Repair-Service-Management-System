package com.nipuna.demo.service.customer;

import com.nipuna.demo.dto.*;
import com.nipuna.demo.dto.customer.CustomerProfileDto;
import com.nipuna.demo.dto.customer.UpdateProfileDto;
import com.nipuna.demo.dto.vehical.VehicleRequestDto;
import com.nipuna.demo.dto.vehical.VehicleResponseDto;
import com.nipuna.demo.dto.repair.RepairRequestDto;
import com.nipuna.demo.dto.repair.RepairResponseDto;
import com.nipuna.demo.entity.User;
import com.nipuna.demo.entity.Vehicle;
import com.nipuna.demo.entity.Repair;
import com.nipuna.demo.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CustomerService {

    private final VehicleRepository vehicleRepository;
    private final RepairRepository repairRepository;
    private final PaymentRepository paymentRepository;
    private final FeedbackRepository feedbackRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    // ===== PROFILE MANAGEMENT =====

    @Transactional(readOnly = true)
    public CustomerProfileDto getProfile(Authentication authentication) {
        User customer = getAuthenticatedCustomer(authentication);
        return mapToProfileDto(customer);
    }

    @Transactional
    public CustomerProfileDto updateProfile(UpdateProfileDto updateDto, Authentication authentication) {
        User customer = getAuthenticatedCustomer(authentication);

        // Validate email uniqueness if changed
        if (updateDto.getEmail() != null && !updateDto.getEmail().equals(customer.getEmail())) {
            if (userRepository.existsByEmail(updateDto.getEmail())) {
                throw new IllegalArgumentException("Email already in use by another account");
            }
            customer.setEmail(updateDto.getEmail());
        }

        // Update profile fields
        if (updateDto.getFullName() != null && !updateDto.getFullName().trim().isEmpty()) {
            customer.setFullName(updateDto.getFullName());
        }

        if (updateDto.getPhone() != null) {
            customer.setPhoneNumber(updateDto.getPhone());
        }

        if (updateDto.getAddress() != null) {
            customer.setAddress(updateDto.getAddress());
        }

        User updated = userRepository.save(customer);
        return mapToProfileDto(updated);
    }

    @Transactional
    public void changePassword(ChangePasswordDto passwordDto, Authentication authentication) {
        User customer = getAuthenticatedCustomer(authentication);

        // Validate current password
        if (!passwordEncoder.matches(passwordDto.getCurrentPassword(), customer.getPassword())) {
            throw new IllegalArgumentException("Current password is incorrect");
        }

        // Validate new password confirmation
        if (!passwordDto.getNewPassword().equals(passwordDto.getConfirmPassword())) {
            throw new IllegalArgumentException("New password and confirmation do not match");
        }

        // Validate new password strength
        if (passwordDto.getNewPassword().length() < 6) {
            throw new IllegalArgumentException("New password must be at least 6 characters long");
        }

        // Ensure new password is different from current
        if (passwordEncoder.matches(passwordDto.getNewPassword(), customer.getPassword())) {
            throw new IllegalArgumentException("New password must be different from current password");
        }

        // Update password
        customer.setPassword(passwordEncoder.encode(passwordDto.getNewPassword()));
        userRepository.save(customer);
    }

    // ===== VEHICLE MANAGEMENT =====

    // Add a new vehicle for a specific customer
    @Transactional
    public VehicleResponseDto addVehicle(VehicleRequestDto vehicleRequestDto, Authentication authentication) {
        User customer = getAuthenticatedCustomer(authentication);

        // Check if vehicle with the same number already exists for this customer
        if (vehicleRepository.existsByVehicleNumberAndCustomerId(
                vehicleRequestDto.getVehicleNumber(), customer.getId())) {
            throw new RuntimeException("Vehicle with number " + vehicleRequestDto.getVehicleNumber()
                    + " already exists for this customer");
        }

        // Create new vehicle entity
        Vehicle vehicle = new Vehicle();
        vehicle.setVehicleNumber(vehicleRequestDto.getVehicleNumber());
        vehicle.setMake(vehicleRequestDto.getMake());
        vehicle.setModel(vehicleRequestDto.getModel());
        vehicle.setYear(vehicleRequestDto.getYear());
        vehicle.setVehicleType(vehicleRequestDto.getVehicleType());
        vehicle.setCustomer(customer);

        // Save vehicle to database
        Vehicle savedVehicle = vehicleRepository.save(vehicle);

        // Convert entity to DTO and return
        return mapToVehicleResponseDto(savedVehicle);
    }

    // Get all vehicles for the authenticated customer
    @Transactional(readOnly = true)
    public List<VehicleResponseDto> getMyVehicles(Authentication authentication) {
        User customer = getAuthenticatedCustomer(authentication);

        // Fetch vehicles by customer ID and convert to DTOs
        return vehicleRepository.findByCustomerId(customer.getId()).stream()
                .map(this::mapToVehicleResponseDto)
                .collect(Collectors.toList());
    }

    // Get a specific vehicle by its ID
    @Transactional(readOnly = true)
    public VehicleResponseDto getVehicleById(Long vehicleId, Authentication authentication) {
        User customer = getAuthenticatedCustomer(authentication);

        // Find vehicle by ID
        Vehicle vehicle = vehicleRepository.findById(vehicleId)
                .orElseThrow(() -> new RuntimeException("Vehicle not found with id: " + vehicleId));

        // Verify ownership
        if (!vehicle.getCustomer().getId().equals(customer.getId())) {
            throw new RuntimeException("You are not authorized to access this vehicle");
        }

        // Convert to DTO and return
        return mapToVehicleResponseDto(vehicle);
    }

    // Update vehicle details
    @Transactional
    public VehicleResponseDto updateVehicle(Long vehicleId, VehicleRequestDto vehicleRequestDto,
                                           Authentication authentication) {
        User customer = getAuthenticatedCustomer(authentication);

        // Find the vehicle by ID
        Vehicle vehicle = vehicleRepository.findById(vehicleId)
                .orElseThrow(() -> new RuntimeException("Vehicle not found with id: " + vehicleId));

        // Check if the vehicle belongs to the customer (authorization)
        if (!vehicle.getCustomer().getId().equals(customer.getId())) {
            throw new RuntimeException("You are not authorized to update this vehicle");
        }

        // Check if the new vehicle number conflicts with another vehicle
        if (!vehicle.getVehicleNumber().equals(vehicleRequestDto.getVehicleNumber()) &&
                vehicleRepository.existsByVehicleNumberAndCustomerId(
                        vehicleRequestDto.getVehicleNumber(), customer.getId())) {
            throw new RuntimeException("Vehicle with number " + vehicleRequestDto.getVehicleNumber()
                    + " already exists for this customer");
        }

        // Update vehicle fields
        vehicle.setVehicleNumber(vehicleRequestDto.getVehicleNumber());
        vehicle.setMake(vehicleRequestDto.getMake());
        vehicle.setModel(vehicleRequestDto.getModel());
        vehicle.setYear(vehicleRequestDto.getYear());
        vehicle.setVehicleType(vehicleRequestDto.getVehicleType());

        // Save updated vehicle
        Vehicle updatedVehicle = vehicleRepository.save(vehicle);

        // Convert to DTO and return
        return mapToVehicleResponseDto(updatedVehicle);
    }

    // Delete a vehicle by its ID
    @Transactional
    public void deleteVehicle(Long vehicleId, Authentication authentication) {
        User customer = getAuthenticatedCustomer(authentication);

        // Find the vehicle by ID
        Vehicle vehicle = vehicleRepository.findById(vehicleId)
                .orElseThrow(() -> new RuntimeException("Vehicle not found with id: " + vehicleId));

        // Check if the vehicle belongs to the customer (authorization)
        if (!vehicle.getCustomer().getId().equals(customer.getId())) {
            throw new RuntimeException("You are not authorized to delete this vehicle");
        }

        // Delete the vehicle from database
        vehicleRepository.delete(vehicle);
    }

    // ===== REPAIR / SERVICE REQUEST MANAGEMENT =====

    // Create a repair/service request
    @Transactional
    public RepairResponseDto createRepairRequest(RepairRequestDto requestDto, Authentication authentication) {
        User customer = getAuthenticatedCustomer(authentication);

        // Validate vehicle ownership
        Vehicle vehicle = vehicleRepository.findById(requestDto.getVehicleId())
                .orElseThrow(() -> new RuntimeException("Vehicle not found with id: " + requestDto.getVehicleId()));

        if (!vehicle.getCustomer().getId().equals(customer.getId())) {
            throw new RuntimeException("Access denied. You can only create repair requests for your own vehicles");
        }

        // Validate input (issue description, service type)
        validateRepairRequestInput(requestDto);

        // Prevent multiple active repair requests for the same vehicle
        preventMultipleActiveRepairRequests(requestDto.getVehicleId());

        // Create new repair entity
        Repair repair = new Repair();
        repair.setVehicle(vehicle);
        repair.setCustomer(customer);
        repair.setServiceType(requestDto.getServiceType());
        repair.setIssueDescription(requestDto.getIssueDescription());

        // Set initial status to REQUESTED and payment status to PENDING
        repair.setStatus(Repair.RepairStatus.REQUESTED);
        repair.setPaymentStatus(Repair.PaymentStatus.PENDING);

        // Auto-generate repair request number
        repair.setRepairRequestNumber(generateRepairRequestNumber());

        // Assign repair priority based on service type
        if (requestDto.getPriority() != null) {
            repair.setPriority(requestDto.getPriority());
        } else {
            repair.setPriority(assignPriorityByServiceType(requestDto.getServiceType()));
        }

        // Save repair to database
        Repair savedRepair = repairRepository.save(repair);

        // Convert to DTO and return
        return mapToRepairResponseDto(savedRepair);
    }

    // Get all repair requests for the authenticated customer
    @Transactional(readOnly = true)
    public List<RepairResponseDto> getMyRepairRequests(Authentication authentication) {
        User customer = getAuthenticatedCustomer(authentication);

        // Find all repair requests for this customer
        List<Repair> repairs = repairRepository.findByCustomerId(customer.getId());

        // Convert to DTOs and return
        return repairs.stream()
                .map(this::mapToRepairResponseDto)
                .collect(Collectors.toList());
    }

    // Get a specific repair request by ID
    @Transactional(readOnly = true)
    public RepairResponseDto getRepairRequestById(Long repairId, Authentication authentication) {
        User customer = getAuthenticatedCustomer(authentication);

        // Find the repair by ID
        Repair repair = repairRepository.findById(repairId)
                .orElseThrow(() -> new RuntimeException("Repair not found with id: " + repairId));

        // Verify the repair belongs to the customer
        if (!repair.getCustomer().getId().equals(customer.getId())) {
            throw new RuntimeException("Access denied. You can only view your own repair requests");
        }

        // Convert to DTO and return
        return mapToRepairResponseDto(repair);
    }

    // Handle cost estimate approval/rejection
    @Transactional
    public RepairResponseDto handleCostEstimateApproval(Long repairId, boolean approved,
                                                       Authentication authentication) {
        User customer = getAuthenticatedCustomer(authentication);

        // Find the repair by ID
        Repair repair = repairRepository.findById(repairId)
                .orElseThrow(() -> new RuntimeException("Repair not found with id: " + repairId));

        // Verify the repair belongs to the customer
        if (!repair.getCustomer().getId().equals(customer.getId())) {
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
            repair.setEstimateApproved(true);
            repair.setStatus(Repair.RepairStatus.APPROVED);
        } else {
            repair.setEstimateApproved(false);
            repair.setStatus(Repair.RepairStatus.CANCELLED);
            repair.setCancellationReason("Customer rejected the cost estimate");
            repair.setCancelledAt(java.time.LocalDateTime.now());
        }

        // Save updated repair
        Repair updatedRepair = repairRepository.save(repair);

        // Convert to DTO and return
        return mapToRepairResponseDto(updatedRepair);
    }

    // Cancel a repair request (only REQUESTED or ASSIGNED status)
    @Transactional
    public RepairResponseDto cancelRepairRequest(Long repairId, String cancellationReason,
                                                Authentication authentication) {
        User customer = getAuthenticatedCustomer(authentication);

        // Find the repair by ID
        Repair repair = repairRepository.findById(repairId)
                .orElseThrow(() -> new RuntimeException("Repair not found with id: " + repairId));

        // Verify the repair belongs to the customer
        if (!repair.getCustomer().getId().equals(customer.getId())) {
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
        return mapToRepairResponseDto(updatedRepair);
    }

    // Get complete repair history for authenticated customer
    @Transactional(readOnly = true)
    public List<RepairResponseDto> getMyRepairHistory(Authentication authentication) {
        User customer = getAuthenticatedCustomer(authentication);

        // Find all repair requests for this customer (all statuses - complete history)
        List<Repair> repairs = repairRepository.findByCustomerId(customer.getId());

        // Sort by creation date (newest first) and convert to DTOs
        return repairs.stream()
                .sorted((r1, r2) -> r2.getCreatedAt().compareTo(r1.getCreatedAt()))
                .map(this::mapToRepairResponseDto)
                .collect(Collectors.toList());
    }

    // Get repair history for a specific vehicle
    @Transactional(readOnly = true)
    public List<RepairResponseDto> getVehicleRepairHistory(Long vehicleId, Authentication authentication) {
        User customer = getAuthenticatedCustomer(authentication);

        // Find the vehicle by ID
        Vehicle vehicle = vehicleRepository.findById(vehicleId)
                .orElseThrow(() -> new RuntimeException("Vehicle not found with id: " + vehicleId));

        // Verify the vehicle belongs to the customer
        if (!vehicle.getCustomer().getId().equals(customer.getId())) {
            throw new RuntimeException("Access denied. You can only view repair history for your own vehicles");
        }

        // Find all repair requests for this vehicle (all statuses - complete history)
        List<Repair> repairs = repairRepository.findByVehicleId(vehicleId);

        // Sort by creation date (newest first) and convert to DTOs
        return repairs.stream()
                .sorted((r1, r2) -> r2.getCreatedAt().compareTo(r1.getCreatedAt()))
                .map(this::mapToRepairResponseDto)
                .collect(Collectors.toList());
    }

    // ===== PRIVATE HELPER METHODS =====

    // Validate repair request input
    private void validateRepairRequestInput(RepairRequestDto requestDto) {
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

    // Prevent multiple active repair requests for the same vehicle
    private void preventMultipleActiveRepairRequests(Long vehicleId) {
        List<Repair.RepairStatus> activeStatuses = Arrays.asList(
                Repair.RepairStatus.REQUESTED,
                Repair.RepairStatus.ASSIGNED,
                Repair.RepairStatus.IN_PROGRESS,
                Repair.RepairStatus.ESTIMATE_SUBMITTED,
                Repair.RepairStatus.APPROVED
        );

        boolean hasActiveRepair = repairRepository.existsByVehicleIdAndStatusIn(vehicleId, activeStatuses);

        if (hasActiveRepair) {
            throw new RuntimeException("This vehicle already has an active repair request. Please wait until the current repair is completed.");
        }
    }

    // Auto-generate repair request number (format: RR-YYYYMMDD-XXXX)
    private String generateRepairRequestNumber() {
        String datePart = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        String prefix = "RR-" + datePart + "-";

        long totalRepairs = repairRepository.count();
        String sequenceNumber = String.format("%04d", totalRepairs + 1);

        return prefix + sequenceNumber;
    }

    // Assign repair priority based on service type
    private Repair.RepairPriority assignPriorityByServiceType(Repair.ServiceType serviceType) {
        switch (serviceType) {
            case BREAKDOWN:
                return Repair.RepairPriority.URGENT;
            case ENGINE_REPAIR:
            case ELECTRICAL:
                return Repair.RepairPriority.HIGH;
            case BODY_REPAIR:
            case TIRE_SERVICE:
            case INSPECTION:
                return Repair.RepairPriority.NORMAL;
            case REGULAR_SERVICE:
            case OTHER:
            default:
                return Repair.RepairPriority.LOW;
        }
    }

    // ...existing code for payment management...

    // ...existing code for history...

    // ...existing code for feedback...

    // ===== HELPER METHODS =====

    private User getAuthenticatedCustomer(Authentication authentication) {
        String username = authentication.getName();
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));
    }

    private CustomerProfileDto mapToProfileDto(User user) {
        return new CustomerProfileDto(
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                user.getFullName(),
                user.getPhoneNumber(),
                user.getAddress()
        );
    }

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

    // ...existing helper methods...
}

