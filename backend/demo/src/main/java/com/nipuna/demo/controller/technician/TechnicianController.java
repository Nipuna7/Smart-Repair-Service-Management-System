package com.nipuna.demo.controller.technician;

import com.nipuna.demo.dto.repair.RepairResponseDto;
import com.nipuna.demo.dto.vehical.VehicleResponseDto;
import com.nipuna.demo.service.technician.TechnicianService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/technician")
@CrossOrigin(origins = "*", maxAge = 3600)
@RequiredArgsConstructor
public class TechnicianController {

    private final TechnicianService technicianService;

    // ===== DASHBOARD =====

    // Access the technician dashboard to verify authentication works
    @GetMapping("/dashboard")
    public ResponseEntity<?> getDashboard(Authentication authentication) {
        Map<String, Object> response = new HashMap<>();
        response.put("message", "Welcome to Technician Dashboard");
        response.put("user", authentication.getName());
        response.put("authorities", authentication.getAuthorities());
        return ResponseEntity.ok(response);
    }

    // ===== ASSIGNED REPAIR MANAGEMENT =====

    // Get list of all assigned repair requests
    @GetMapping("/repairs/assigned")
    public ResponseEntity<List<RepairResponseDto>> getAssignedRepairs(Authentication authentication) {
        List<RepairResponseDto> repairs = technicianService.getAssignedRepairs(authentication);
        return ResponseEntity.ok(repairs);
    }

    // Get detailed information about an assigned repair
    @GetMapping("/repairs/{id}/details")
    public ResponseEntity<Map<String, Object>> getAssignedRepairDetails(
            @PathVariable Long id,
            Authentication authentication) {
        Map<String, Object> details = technicianService.getAssignedRepairDetails(id, authentication);
        return ResponseEntity.ok(details);
    }

    // Get repair history for the vehicle
    @GetMapping("/repairs/{id}/vehicle-history")
    public ResponseEntity<List<RepairResponseDto>> getVehicleRepairHistory(
            @PathVariable Long id,
            Authentication authentication) {
        List<RepairResponseDto> history = technicianService.getVehicleRepairHistory(id, authentication);
        return ResponseEntity.ok(history);
    }

    // Get vehicle information for an assigned repair
    @GetMapping("/repairs/{id}/vehicle-info")
    public ResponseEntity<VehicleResponseDto> getVehicleInfo(
            @PathVariable Long id,
            Authentication authentication) {
        VehicleResponseDto vehicleInfo = technicianService.getVehicleInfo(id, authentication);
        return ResponseEntity.ok(vehicleInfo);
    }

    // Get customer contact information for an assigned repair
    @GetMapping("/repairs/{id}/customer-contact")
    public ResponseEntity<Map<String, Object>> getCustomerContact(
            @PathVariable Long id,
            Authentication authentication) {
        Map<String, Object> contact = technicianService.getCustomerContact(id, authentication);
        return ResponseEntity.ok(contact);
    }

    // ===== REPAIR DIAGNOSIS & NOTES =====

    // Add diagnosis details to a repair
    @PostMapping("/repairs/{id}/diagnosis")
    public ResponseEntity<RepairResponseDto> addDiagnosisDetails(
            @PathVariable Long id,
            @RequestBody Map<String, String> request,
            Authentication authentication) {
        String diagnosisDetails = request.get("diagnosisDetails");
        RepairResponseDto response = technicianService.addDiagnosisDetails(id, diagnosisDetails, authentication);
        return ResponseEntity.ok(response);
    }

    // Add repair notes
    @PostMapping("/repairs/{id}/notes")
    public ResponseEntity<RepairResponseDto> addRepairNotes(
            @PathVariable Long id,
            @RequestBody Map<String, String> request,
            Authentication authentication) {
        String repairNotes = request.get("repairNotes");
        RepairResponseDto response = technicianService.addRepairNotes(id, repairNotes, authentication);
        return ResponseEntity.ok(response);
    }

    // Get diagnosis details
    @GetMapping("/repairs/{id}/diagnosis")
    public ResponseEntity<Map<String, Object>> getDiagnosisDetails(
            @PathVariable Long id,
            Authentication authentication) {
        Map<String, Object> diagnosis = technicianService.getDiagnosisDetails(id, authentication);
        return ResponseEntity.ok(diagnosis);
    }

    // Get repair notes
    @GetMapping("/repairs/{id}/notes")
    public ResponseEntity<Map<String, Object>> getRepairNotes(
            @PathVariable Long id,
            Authentication authentication) {
        Map<String, Object> notes = technicianService.getRepairNotes(id, authentication);
        return ResponseEntity.ok(notes);
    }

    // ===== COST ESTIMATION =====

    // Submit cost estimate for a repair
    @PostMapping("/repairs/{id}/estimate")
    public ResponseEntity<RepairResponseDto> submitCostEstimate(
            @PathVariable Long id,
            @RequestBody Map<String, Object> request,
            Authentication authentication) {
        java.math.BigDecimal estimatedCost = new java.math.BigDecimal(request.get("estimatedCost").toString());
        RepairResponseDto response = technicianService.submitCostEstimate(id, estimatedCost, authentication);
        return ResponseEntity.ok(response);
    }

    // Update cost estimate (if customer rejected)
    @PutMapping("/repairs/{id}/estimate")
    public ResponseEntity<RepairResponseDto> updateCostEstimate(
            @PathVariable Long id,
            @RequestBody Map<String, Object> request,
            Authentication authentication) {
        java.math.BigDecimal newEstimatedCost = new java.math.BigDecimal(request.get("estimatedCost").toString());
        RepairResponseDto response = technicianService.updateCostEstimate(id, newEstimatedCost, authentication);
        return ResponseEntity.ok(response);
    }

    // Start repair work (mark as IN_PROGRESS)
    @PostMapping("/repairs/{id}/start-work")
    public ResponseEntity<RepairResponseDto> startRepairWork(
            @PathVariable Long id,
            Authentication authentication) {
        RepairResponseDto response = technicianService.startRepairWork(id, authentication);
        return ResponseEntity.ok(response);
    }

    // Check customer approval status
    @GetMapping("/repairs/{id}/approval-status")
    public ResponseEntity<Map<String, Object>> checkApprovalStatus(
            @PathVariable Long id,
            Authentication authentication) {
        Map<String, Object> status = technicianService.checkApprovalStatus(id, authentication);
        return ResponseEntity.ok(status);
    }

    // ===== REPAIR STATUS UPDATE =====

    // Update repair status from ASSIGNED/APPROVED to IN_PROGRESS
    @PutMapping("/repairs/{id}/status/in-progress")
    public ResponseEntity<RepairResponseDto> updateStatusToInProgress(
            @PathVariable Long id,
            Authentication authentication) {
        RepairResponseDto response = technicianService.updateStatusToInProgress(id, authentication);
        return ResponseEntity.ok(response);
    }

    // Update repair status to WAITING_FOR_PARTS
    @PutMapping("/repairs/{id}/status/waiting-for-parts")
    public ResponseEntity<RepairResponseDto> updateStatusToWaitingForParts(
            @PathVariable Long id,
            @RequestBody Map<String, String> request,
            Authentication authentication) {
        String reason = request.get("reason");
        RepairResponseDto response = technicianService.updateStatusToWaitingForParts(id, reason, authentication);
        return ResponseEntity.ok(response);
    }

    // Mark repair as COMPLETED
    @PutMapping("/repairs/{id}/status/completed")
    public ResponseEntity<RepairResponseDto> markRepairAsCompleted(
            @PathVariable Long id,
            @RequestBody Map<String, Object> request,
            Authentication authentication) {
        java.math.BigDecimal finalCost = new java.math.BigDecimal(request.get("finalCost").toString());
        RepairResponseDto response = technicianService.markRepairAsCompleted(id, finalCost, authentication);
        return ResponseEntity.ok(response);
    }

    // Get repair status workflow (current status and allowed next actions)
    @GetMapping("/repairs/{id}/status/workflow")
    public ResponseEntity<Map<String, Object>> getRepairStatusWorkflow(
            @PathVariable Long id,
            Authentication authentication) {
        Map<String, Object> workflow = technicianService.getRepairStatusWorkflow(id, authentication);
        return ResponseEntity.ok(workflow);
    }

    // ===== LEGACY ENDPOINT (Keep for compatibility) =====

    // Get assigned tasks (alias for assigned repairs)
    @GetMapping("/tasks")
    public ResponseEntity<List<RepairResponseDto>> getTasks(Authentication authentication) {
        List<RepairResponseDto> repairs = technicianService.getAssignedRepairs(authentication);
        return ResponseEntity.ok(repairs);
    }
}

