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

import java.math.BigDecimal;
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

    // ===== REPAIR DIAGNOSIS & NOTES SERVICE =====

    // Add diagnosis details to a repair
    @Transactional
    public RepairResponseDto addDiagnosisDetails(Long repairId, String diagnosisDetails, Authentication authentication) {
        User technician = getAuthenticatedTechnician(authentication);

        // Find the repair
        Repair repair = repairRepository.findById(repairId)
                .orElseThrow(() -> new RuntimeException("Repair not found with id: " + repairId));

        // Verify the repair is assigned to this technician
        if (repair.getTechnician() == null ||
            !repair.getTechnician().getId().equals(technician.getId())) {
            throw new RuntimeException("Access denied. This repair is not assigned to you");
        }

        // Check if repair is completed (read-only after completion)
        if (repair.getStatus() == Repair.RepairStatus.COMPLETED ||
            repair.getStatus() == Repair.RepairStatus.DELIVERED) {
            throw new RuntimeException("Cannot modify diagnosis. Repair is already completed and notes are read-only");
        }

        // Validate diagnosis details
        if (diagnosisDetails == null || diagnosisDetails.trim().isEmpty()) {
            throw new IllegalArgumentException("Diagnosis details cannot be empty");
        }

        if (diagnosisDetails.trim().length() < 10) {
            throw new IllegalArgumentException("Diagnosis details must be at least 10 characters long");
        }

        if (diagnosisDetails.trim().length() > 5000) {
            throw new IllegalArgumentException("Diagnosis details must not exceed 5000 characters");
        }

        // Set diagnosis details
        repair.setDiagnosisDetails(diagnosisDetails.trim());

        // Save repair
        Repair updatedRepair = repairRepository.save(repair);

        // Convert to DTO and return
        return mapToRepairResponseDto(updatedRepair);
    }

    // Add internal repair notes
    @Transactional
    public RepairResponseDto addRepairNotes(Long repairId, String repairNotes, Authentication authentication) {
        User technician = getAuthenticatedTechnician(authentication);

        // Find the repair
        Repair repair = repairRepository.findById(repairId)
                .orElseThrow(() -> new RuntimeException("Repair not found with id: " + repairId));

        // Verify the repair is assigned to this technician
        if (repair.getTechnician() == null ||
            !repair.getTechnician().getId().equals(technician.getId())) {
            throw new RuntimeException("Access denied. This repair is not assigned to you");
        }

        // Check if repair is completed (read-only after completion)
        if (repair.getStatus() == Repair.RepairStatus.COMPLETED ||
            repair.getStatus() == Repair.RepairStatus.DELIVERED) {
            throw new RuntimeException("Cannot modify notes. Repair is already completed and notes are read-only");
        }

        // Validate repair notes
        if (repairNotes == null || repairNotes.trim().isEmpty()) {
            throw new IllegalArgumentException("Repair notes cannot be empty");
        }

        if (repairNotes.trim().length() < 10) {
            throw new IllegalArgumentException("Repair notes must be at least 10 characters long");
        }

        if (repairNotes.trim().length() > 5000) {
            throw new IllegalArgumentException("Repair notes must not exceed 5000 characters");
        }

        // Append new notes to existing notes if any
        String currentNotes = repair.getRepairNotes();
        String updatedNotes;

        if (currentNotes != null && !currentNotes.trim().isEmpty()) {
            // Append with timestamp
            updatedNotes = currentNotes + "\n\n--- " + java.time.LocalDateTime.now() + " ---\n" + repairNotes.trim();
        } else {
            updatedNotes = repairNotes.trim();
        }

        // Set repair notes
        repair.setRepairNotes(updatedNotes);

        // Save repair
        Repair updatedRepair = repairRepository.save(repair);

        // Convert to DTO and return
        return mapToRepairResponseDto(updatedRepair);
    }

    // Get diagnosis details for a repair
    @Transactional(readOnly = true)
    public Map<String, Object> getDiagnosisDetails(Long repairId, Authentication authentication) {
        User technician = getAuthenticatedTechnician(authentication);

        // Find the repair
        Repair repair = repairRepository.findById(repairId)
                .orElseThrow(() -> new RuntimeException("Repair not found with id: " + repairId));

        // Verify the repair is assigned to this technician
        if (repair.getTechnician() == null ||
            !repair.getTechnician().getId().equals(technician.getId())) {
            throw new RuntimeException("Access denied. This repair is not assigned to you");
        }

        // Build diagnosis information
        Map<String, Object> diagnosisInfo = new HashMap<>();
        diagnosisInfo.put("repairId", repair.getId());
        diagnosisInfo.put("repairRequestNumber", repair.getRepairRequestNumber());
        diagnosisInfo.put("status", repair.getStatus());
        diagnosisInfo.put("diagnosisDetails", repair.getDiagnosisDetails());
        diagnosisInfo.put("hasDiagnosis", repair.getDiagnosisDetails() != null && !repair.getDiagnosisDetails().isEmpty());
        diagnosisInfo.put("isReadOnly", repair.getStatus() == Repair.RepairStatus.COMPLETED ||
                                         repair.getStatus() == Repair.RepairStatus.DELIVERED);

        return diagnosisInfo;
    }

    // Get repair notes
    @Transactional(readOnly = true)
    public Map<String, Object> getRepairNotes(Long repairId, Authentication authentication) {
        User technician = getAuthenticatedTechnician(authentication);

        // Find the repair
        Repair repair = repairRepository.findById(repairId)
                .orElseThrow(() -> new RuntimeException("Repair not found with id: " + repairId));

        // Verify the repair is assigned to this technician
        if (repair.getTechnician() == null ||
            !repair.getTechnician().getId().equals(technician.getId())) {
            throw new RuntimeException("Access denied. This repair is not assigned to you");
        }

        // Build notes information
        Map<String, Object> notesInfo = new HashMap<>();
        notesInfo.put("repairId", repair.getId());
        notesInfo.put("repairRequestNumber", repair.getRepairRequestNumber());
        notesInfo.put("status", repair.getStatus());
        notesInfo.put("repairNotes", repair.getRepairNotes());
        notesInfo.put("hasNotes", repair.getRepairNotes() != null && !repair.getRepairNotes().isEmpty());
        notesInfo.put("isReadOnly", repair.getStatus() == Repair.RepairStatus.COMPLETED ||
                                     repair.getStatus() == Repair.RepairStatus.DELIVERED);

        return notesInfo;
    }

    // ===== COST ESTIMATION SERVICE =====

    // Submit cost estimate for a repair
    @Transactional
    public RepairResponseDto submitCostEstimate(Long repairId, java.math.BigDecimal estimatedCost, Authentication authentication) {
        User technician = getAuthenticatedTechnician(authentication);

        // Find the repair
        Repair repair = repairRepository.findById(repairId)
                .orElseThrow(() -> new RuntimeException("Repair not found with id: " + repairId));

        // Verify the repair is assigned to this technician
        if (repair.getTechnician() == null ||
            !repair.getTechnician().getId().equals(technician.getId())) {
            throw new RuntimeException("Access denied. This repair is not assigned to you");
        }

        // Validate estimated cost
        if (estimatedCost == null || estimatedCost.compareTo(java.math.BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Estimated cost must be greater than zero");
        }

        if (estimatedCost.compareTo(new java.math.BigDecimal("999999.99")) > 0) {
            throw new IllegalArgumentException("Estimated cost exceeds maximum allowed amount");
        }

        // Check if repair status allows estimate submission
        if (repair.getStatus() != Repair.RepairStatus.ASSIGNED &&
            repair.getStatus() != Repair.RepairStatus.IN_PROGRESS &&
            repair.getStatus() != Repair.RepairStatus.ESTIMATE_SUBMITTED) {
            throw new RuntimeException("Cannot submit estimate. Repair status must be ASSIGNED, IN_PROGRESS, or ESTIMATE_SUBMITTED");
        }

        // Set estimated cost and update status
        repair.setEstimatedCost(estimatedCost);
        repair.setStatus(Repair.RepairStatus.ESTIMATE_SUBMITTED);
        repair.setEstimateApproved(null); // Reset approval status

        // Save repair
        Repair updatedRepair = repairRepository.save(repair);

        // Convert to DTO and return
        return mapToRepairResponseDto(updatedRepair);
    }

    // Update cost estimate (if customer rejected previous estimate)
    @Transactional
    public RepairResponseDto updateCostEstimate(Long repairId, java.math.BigDecimal newEstimatedCost, Authentication authentication) {
        User technician = getAuthenticatedTechnician(authentication);

        // Find the repair
        Repair repair = repairRepository.findById(repairId)
                .orElseThrow(() -> new RuntimeException("Repair not found with id: " + repairId));

        // Verify the repair is assigned to this technician
        if (repair.getTechnician() == null ||
            !repair.getTechnician().getId().equals(technician.getId())) {
            throw new RuntimeException("Access denied. This repair is not assigned to you");
        }

        // Check if previous estimate was rejected
        if (repair.getEstimateApproved() == null || repair.getEstimateApproved() == true) {
            throw new RuntimeException("Can only update estimate if previous estimate was rejected by customer");
        }

        // Validate new estimated cost
        if (newEstimatedCost == null || newEstimatedCost.compareTo(java.math.BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Estimated cost must be greater than zero");
        }

        if (newEstimatedCost.compareTo(new java.math.BigDecimal("999999.99")) > 0) {
            throw new IllegalArgumentException("Estimated cost exceeds maximum allowed amount");
        }

        // Update estimated cost and reset approval
        repair.setEstimatedCost(newEstimatedCost);
        repair.setStatus(Repair.RepairStatus.ESTIMATE_SUBMITTED);
        repair.setEstimateApproved(null); // Reset approval status for new estimate

        // Save repair
        Repair updatedRepair = repairRepository.save(repair);

        // Convert to DTO and return
        return mapToRepairResponseDto(updatedRepair);
    }

    // Mark repair as IN_PROGRESS (only allowed after customer approval)
    @Transactional
    public RepairResponseDto startRepairWork(Long repairId, Authentication authentication) {
        User technician = getAuthenticatedTechnician(authentication);

        // Find the repair
        Repair repair = repairRepository.findById(repairId)
                .orElseThrow(() -> new RuntimeException("Repair not found with id: " + repairId));

        // Verify the repair is assigned to this technician
        if (repair.getTechnician() == null ||
            !repair.getTechnician().getId().equals(technician.getId())) {
            throw new RuntimeException("Access denied. This repair is not assigned to you");
        }

        // IMPORTANT RULE: Cannot proceed without customer approval
        if (repair.getEstimateApproved() == null || repair.getEstimateApproved() == false) {
            throw new RuntimeException("Cannot start repair work. Customer must approve the cost estimate first");
        }

        // Check if repair status is APPROVED
        if (repair.getStatus() != Repair.RepairStatus.APPROVED) {
            throw new RuntimeException("Cannot start repair. Status must be APPROVED");
        }

        // Update status to IN_PROGRESS and set timestamp
        repair.setStatus(Repair.RepairStatus.IN_PROGRESS);
        repair.setInProgressAt(java.time.LocalDateTime.now());

        // Save repair
        Repair updatedRepair = repairRepository.save(repair);

        // Convert to DTO and return
        return mapToRepairResponseDto(updatedRepair);
    }

    // Check if technician can start work (customer approval status)
    @Transactional(readOnly = true)
    public Map<String, Object> checkApprovalStatus(Long repairId, Authentication authentication) {
        User technician = getAuthenticatedTechnician(authentication);

        // Find the repair
        Repair repair = repairRepository.findById(repairId)
                .orElseThrow(() -> new RuntimeException("Repair not found with id: " + repairId));

        // Verify the repair is assigned to this technician
        if (repair.getTechnician() == null ||
            !repair.getTechnician().getId().equals(technician.getId())) {
            throw new RuntimeException("Access denied. This repair is not assigned to you");
        }

        // Build approval status information
        Map<String, Object> approvalStatus = new HashMap<>();
        approvalStatus.put("repairId", repair.getId());
        approvalStatus.put("repairRequestNumber", repair.getRepairRequestNumber());
        approvalStatus.put("status", repair.getStatus());
        approvalStatus.put("estimatedCost", repair.getEstimatedCost());
        approvalStatus.put("estimateApproved", repair.getEstimateApproved());

        // Determine if technician can start work
        boolean canStartWork = repair.getEstimateApproved() != null &&
                               repair.getEstimateApproved() == true &&
                               repair.getStatus() == Repair.RepairStatus.APPROVED;

        approvalStatus.put("canStartWork", canStartWork);

        // Provide reason if cannot start
        if (!canStartWork) {
            if (repair.getEstimatedCost() == null) {
                approvalStatus.put("reason", "Cost estimate not submitted yet");
            } else if (repair.getEstimateApproved() == null) {
                approvalStatus.put("reason", "Waiting for customer approval");
            } else if (repair.getEstimateApproved() == false) {
                approvalStatus.put("reason", "Customer rejected the estimate. Please update and resubmit");
            } else if (repair.getStatus() != Repair.RepairStatus.APPROVED) {
                approvalStatus.put("reason", "Repair status must be APPROVED to start work");
            }
        } else {
            approvalStatus.put("reason", "Ready to start work");
        }

        return approvalStatus;
    }

    // ===== REPAIR STATUS UPDATE SERVICE =====

    // Move status from ASSIGNED → IN_PROGRESS
    @Transactional
    public RepairResponseDto updateStatusToInProgress(Long repairId, Authentication authentication) {
        User technician = getAuthenticatedTechnician(authentication);

        // Find the repair
        Repair repair = repairRepository.findById(repairId)
                .orElseThrow(() -> new RuntimeException("Repair not found with id: " + repairId));

        // Verify the repair is assigned to this technician
        if (repair.getTechnician() == null ||
            !repair.getTechnician().getId().equals(technician.getId())) {
            throw new RuntimeException("Access denied. This repair is not assigned to you");
        }

        // Validate current status is ASSIGNED or APPROVED
        if (repair.getStatus() != Repair.RepairStatus.ASSIGNED &&
            repair.getStatus() != Repair.RepairStatus.APPROVED) {
            throw new RuntimeException("Cannot move to IN_PROGRESS. Current status must be ASSIGNED or APPROVED");
        }

        // Check if customer approved the estimate (if estimate was submitted)
        if (repair.getEstimatedCost() != null &&
            (repair.getEstimateApproved() == null || repair.getEstimateApproved() == false)) {
            throw new RuntimeException("Cannot start repair work. Customer must approve the cost estimate first");
        }

        // Update status to IN_PROGRESS
        repair.setStatus(Repair.RepairStatus.IN_PROGRESS);
        repair.setInProgressAt(java.time.LocalDateTime.now());

        // Save repair
        Repair updatedRepair = repairRepository.save(repair);

        // Convert to DTO and return
        return mapToRepairResponseDto(updatedRepair);
    }

    // Update to WAITING_FOR_PARTS
    @Transactional
    public RepairResponseDto updateStatusToWaitingForParts(Long repairId, String reason, Authentication authentication) {
        User technician = getAuthenticatedTechnician(authentication);

        // Find the repair
        Repair repair = repairRepository.findById(repairId)
                .orElseThrow(() -> new RuntimeException("Repair not found with id: " + repairId));

        // Verify the repair is assigned to this technician
        if (repair.getTechnician() == null ||
            !repair.getTechnician().getId().equals(technician.getId())) {
            throw new RuntimeException("Access denied. This repair is not assigned to you");
        }

        // Can only mark as waiting for parts if currently IN_PROGRESS
        if (repair.getStatus() != Repair.RepairStatus.IN_PROGRESS) {
            throw new RuntimeException("Cannot mark as WAITING_FOR_PARTS. Repair must be IN_PROGRESS");
        }

        // Validate reason
        if (reason == null || reason.trim().isEmpty()) {
            throw new IllegalArgumentException("Please provide a reason for waiting for parts");
        }

        if (reason.trim().length() < 10) {
            throw new IllegalArgumentException("Reason must be at least 10 characters long");
        }

        // Add waiting reason to repair notes
        String waitingNote = "WAITING FOR PARTS - " + java.time.LocalDateTime.now() +
                            "\nReason: " + reason.trim();

        String currentNotes = repair.getRepairNotes();
        if (currentNotes != null && !currentNotes.trim().isEmpty()) {
            repair.setRepairNotes(currentNotes + "\n\n" + waitingNote);
        } else {
            repair.setRepairNotes(waitingNote);
        }

        // Note: Since WAITING_FOR_PARTS is not in the enum, we'll keep it IN_PROGRESS
        // but document it in notes. If you want a separate status, add it to RepairStatus enum
        // For now, keeping status as IN_PROGRESS with documented reason

        // Save repair
        Repair updatedRepair = repairRepository.save(repair);

        // Convert to DTO and return
        return mapToRepairResponseDto(updatedRepair);
    }

    // Mark repair as COMPLETED
    @Transactional
    public RepairResponseDto markRepairAsCompleted(Long repairId, BigDecimal finalCost, Authentication authentication) {
        User technician = getAuthenticatedTechnician(authentication);

        // Find the repair
        Repair repair = repairRepository.findById(repairId)
                .orElseThrow(() -> new RuntimeException("Repair not found with id: " + repairId));

        // Verify the repair is assigned to this technician
        if (repair.getTechnician() == null ||
            !repair.getTechnician().getId().equals(technician.getId())) {
            throw new RuntimeException("Access denied. This repair is not assigned to you");
        }

        // Can only complete if currently IN_PROGRESS
        if (repair.getStatus() != Repair.RepairStatus.IN_PROGRESS) {
            throw new RuntimeException("Cannot mark as COMPLETED. Repair must be IN_PROGRESS");
        }

        // Validate final cost
        if (finalCost == null || finalCost.compareTo(java.math.BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Final cost must be greater than zero");
        }

        if (finalCost.compareTo(new java.math.BigDecimal("999999.99")) > 0) {
            throw new IllegalArgumentException("Final cost exceeds maximum allowed amount");
        }

        // Validate that diagnosis was provided
        if (repair.getDiagnosisDetails() == null || repair.getDiagnosisDetails().trim().isEmpty()) {
            throw new RuntimeException("Cannot complete repair. Diagnosis details must be provided first");
        }

        // Update status to COMPLETED
        repair.setStatus(Repair.RepairStatus.COMPLETED);
        repair.setCompletedAt(java.time.LocalDateTime.now());
        repair.setFinalCost(finalCost);

        // Save repair
        Repair updatedRepair = repairRepository.save(repair);

        // Convert to DTO and return
        return mapToRepairResponseDto(updatedRepair);
    }

    // Get current status and allowed next actions
    @Transactional(readOnly = true)
    public Map<String, Object> getRepairStatusWorkflow(Long repairId, Authentication authentication) {
        User technician = getAuthenticatedTechnician(authentication);

        // Find the repair
        Repair repair = repairRepository.findById(repairId)
                .orElseThrow(() -> new RuntimeException("Repair not found with id: " + repairId));

        // Verify the repair is assigned to this technician
        if (repair.getTechnician() == null ||
            !repair.getTechnician().getId().equals(technician.getId())) {
            throw new RuntimeException("Access denied. This repair is not assigned to you");
        }

        // Build workflow information
        Map<String, Object> workflow = new HashMap<>();
        workflow.put("repairId", repair.getId());
        workflow.put("repairRequestNumber", repair.getRepairRequestNumber());
        workflow.put("currentStatus", repair.getStatus());

        // Determine allowed next actions based on current status
        java.util.List<String> allowedActions = new java.util.ArrayList<>();

        switch (repair.getStatus()) {
            case ASSIGNED:
            case APPROVED:
                if (repair.getEstimatedCost() != null &&
                    repair.getEstimateApproved() != null &&
                    repair.getEstimateApproved() == true) {
                    allowedActions.add("Move to IN_PROGRESS");
                } else {
                    workflow.put("blockedReason", "Customer must approve estimate first");
                }
                break;

            case IN_PROGRESS:
                allowedActions.add("Mark as WAITING_FOR_PARTS");
                if (repair.getDiagnosisDetails() != null && !repair.getDiagnosisDetails().isEmpty()) {
                    allowedActions.add("Mark as COMPLETED");
                } else {
                    workflow.put("completionBlockedReason", "Diagnosis details required before completion");
                }
                break;

            case COMPLETED:
            case DELIVERED:
                workflow.put("message", "Repair is completed. No further status updates allowed");
                break;

            case CANCELLED:
                workflow.put("message", "Repair is cancelled. No further status updates allowed");
                break;

            default:
                workflow.put("message", "Invalid status for workflow");
                break;
        }

        workflow.put("allowedActions", allowedActions);
        workflow.put("canModify", repair.getStatus() != Repair.RepairStatus.COMPLETED &&
                                   repair.getStatus() != Repair.RepairStatus.DELIVERED &&
                                   repair.getStatus() != Repair.RepairStatus.CANCELLED);

        return workflow;
    }

    // Validate if status transition is allowed
    private boolean isValidStatusTransition(Repair.RepairStatus currentStatus, Repair.RepairStatus newStatus) {
        // Define valid status transitions
        switch (currentStatus) {
            case ASSIGNED:
            case APPROVED:
                return newStatus == Repair.RepairStatus.IN_PROGRESS;

            case IN_PROGRESS:
                return newStatus == Repair.RepairStatus.COMPLETED;

            case COMPLETED:
            case DELIVERED:
            case CANCELLED:
                // Cannot transition from these terminal states
                return false;

            default:
                return false;
        }
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
                // Diagnosis and notes
                .diagnosisDetails(repair.getDiagnosisDetails())
                .repairNotes(repair.getRepairNotes())
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
