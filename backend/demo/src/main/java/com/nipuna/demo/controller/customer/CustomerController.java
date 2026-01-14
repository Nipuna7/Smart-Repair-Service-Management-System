package com.nipuna.demo.controller.customer;

import com.nipuna.demo.service.customer.CustomerService;
import com.nipuna.demo.dto.ChangePasswordDto;
import com.nipuna.demo.dto.MessageResponse;
import com.nipuna.demo.dto.customer.CustomerProfileDto;
import com.nipuna.demo.dto.customer.UpdateProfileDto;
import com.nipuna.demo.dto.vehical.VehicleRequestDto;
import com.nipuna.demo.dto.vehical.VehicleResponseDto;
import com.nipuna.demo.dto.repair.RepairRequestDto;
import com.nipuna.demo.dto.repair.RepairResponseDto;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/customer")
@CrossOrigin(origins = "*", maxAge = 3600)
@RequiredArgsConstructor
public class CustomerController {

    private final CustomerService customerService;

    // ===== DASHBOARD =====

    // Access the customer dashboard to verify authentication works
    @GetMapping("/dashboard")
    public ResponseEntity<?> getDashboard(Authentication authentication) {
        Map<String, Object> response = new HashMap<>();
        response.put("message", "Welcome to Customer Dashboard");
        response.put("user", authentication.getName());
        response.put("authorities", authentication.getAuthorities());
        return ResponseEntity.ok(response);
    }

    // ===== PROFILE MANAGEMENT =====

    // Retrieve the logged-in customer's profile information
    @GetMapping("/profile")
    public ResponseEntity<CustomerProfileDto> getProfile(Authentication authentication) {
        CustomerProfileDto profile = customerService.getProfile(authentication);
        return ResponseEntity.ok(profile);
    }

    // Update the logged-in customer's profile information
    @PutMapping("/profile")
    public ResponseEntity<CustomerProfileDto> updateProfile(
            @RequestBody UpdateProfileDto updateDto,
            Authentication authentication) {
        CustomerProfileDto updated = customerService.updateProfile(updateDto, authentication);
        return ResponseEntity.ok(updated);
    }

    // Change the logged-in customer's password
    @PutMapping("/profile/change-password")
    public ResponseEntity<Map<String, String>> changePassword(
            @RequestBody ChangePasswordDto passwordDto,
            Authentication authentication) {
        customerService.changePassword(passwordDto, authentication);
        Map<String, String> response = new HashMap<>();
        response.put("message", "Password changed successfully");
        return ResponseEntity.ok(response);
    }

    // ===== VEHICLE MANAGEMENT =====

    // Add a new vehicle
    @PostMapping("/vehicles")
    public ResponseEntity<VehicleResponseDto> addVehicle(
            @Valid @RequestBody VehicleRequestDto vehicleRequestDto,
            Authentication authentication) {
        VehicleResponseDto responseDto = customerService.addVehicle(vehicleRequestDto, authentication);
        return ResponseEntity.status(HttpStatus.CREATED).body(responseDto);
    }

    // View all vehicles
    @GetMapping("/vehicles")
    public ResponseEntity<List<VehicleResponseDto>> getMyVehicles(Authentication authentication) {
        List<VehicleResponseDto> vehicles = customerService.getMyVehicles(authentication);
        return ResponseEntity.ok(vehicles);
    }

    // Get a specific vehicle by ID
    @GetMapping("/vehicles/{id}")
    public ResponseEntity<VehicleResponseDto> getVehicleById(
            @PathVariable Long id,
            Authentication authentication) {
        VehicleResponseDto vehicle = customerService.getVehicleById(id, authentication);
        return ResponseEntity.ok(vehicle);
    }

    // Update vehicle details
    @PutMapping("/vehicles/{id}")
    public ResponseEntity<VehicleResponseDto> updateVehicle(
            @PathVariable Long id,
            @Valid @RequestBody VehicleRequestDto vehicleRequestDto,
            Authentication authentication) {
        VehicleResponseDto updatedVehicle = customerService.updateVehicle(id, vehicleRequestDto, authentication);
        return ResponseEntity.ok(updatedVehicle);
    }

    // Delete vehicle
    @DeleteMapping("/vehicles/{id}")
    public ResponseEntity<MessageResponse> deleteVehicle(
            @PathVariable Long id,
            Authentication authentication) {
        customerService.deleteVehicle(id, authentication);
        return ResponseEntity.ok(new MessageResponse("Vehicle deleted successfully"));
    }

    // ===== REPAIR / SERVICE REQUEST MANAGEMENT =====

    // Create a repair/service request
    @PostMapping("/repairs")
    public ResponseEntity<RepairResponseDto> createRepairRequest(
            @Valid @RequestBody RepairRequestDto requestDto,
            Authentication authentication) {
        RepairResponseDto response = customerService.createRepairRequest(requestDto, authentication);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    // View all repair requests
    @GetMapping("/repairs")
    public ResponseEntity<List<RepairResponseDto>> getMyRepairRequests(Authentication authentication) {
        List<RepairResponseDto> repairs = customerService.getMyRepairRequests(authentication);
        return ResponseEntity.ok(repairs);
    }

    // Get a specific repair request by ID
    @GetMapping("/repairs/{id}")
    public ResponseEntity<RepairResponseDto> getRepairRequestById(
            @PathVariable Long id,
            Authentication authentication) {
        RepairResponseDto repair = customerService.getRepairRequestById(id, authentication);
        return ResponseEntity.ok(repair);
    }

    // ===== COST ESTIMATE APPROVAL =====

    // Approve or reject cost estimate
    @PutMapping("/repairs/{id}/approve-estimate")
    public ResponseEntity<RepairResponseDto> handleCostEstimateApproval(
            @PathVariable Long id,
            @RequestParam boolean approved,
            Authentication authentication) {
        RepairResponseDto response = customerService.handleCostEstimateApproval(id, approved, authentication);
        return ResponseEntity.ok(response);
    }

    // ===== CANCELLATION =====

    // Cancel repair request
    @DeleteMapping("/repairs/{id}")
    public ResponseEntity<RepairResponseDto> cancelRepairRequest(
            @PathVariable Long id,
            @RequestParam String cancellationReason,
            Authentication authentication) {
        RepairResponseDto response = customerService.cancelRepairRequest(id, cancellationReason, authentication);
        return ResponseEntity.ok(response);
    }

    // ===== HISTORY =====

    // Get complete repair history
    @GetMapping("/repairs/history")
    public ResponseEntity<List<RepairResponseDto>> getMyRepairHistory(Authentication authentication) {
        List<RepairResponseDto> repairs = customerService.getMyRepairHistory(authentication);
        return ResponseEntity.ok(repairs);
    }

    // Get repair history for a specific vehicle
    @GetMapping("/vehicles/{vehicleId}/repairs")
    public ResponseEntity<List<RepairResponseDto>> getVehicleRepairHistory(
            @PathVariable Long vehicleId,
            Authentication authentication) {
        List<RepairResponseDto> repairs = customerService.getVehicleRepairHistory(vehicleId, authentication);
        return ResponseEntity.ok(repairs);
    }

    // ===== REPAIR TRACKING =====

    // Get current repair status
    @GetMapping("/repairs/{id}/status")
    public ResponseEntity<RepairResponseDto> getRepairStatus(
            @PathVariable Long id,
            Authentication authentication) {
        RepairResponseDto status = customerService.getRepairStatus(id, authentication);
        return ResponseEntity.ok(status);
    }

    // Get repair timeline
    @GetMapping("/repairs/{id}/timeline")
    public ResponseEntity<Map<String, Object>> getRepairTimeline(
            @PathVariable Long id,
            Authentication authentication) {
        Map<String, Object> timeline = customerService.getRepairTimeline(id, authentication);
        return ResponseEntity.ok(timeline);
    }

    // Get assigned technician
    @GetMapping("/repairs/{id}/technician")
    public ResponseEntity<Map<String, Object>> getAssignedTechnician(
            @PathVariable Long id,
            Authentication authentication) {
        Map<String, Object> technician = customerService.getAssignedTechnician(id, authentication);
        return ResponseEntity.ok(technician);
    }

    // ===== COST ESTIMATION & APPROVAL =====

    // Get repair cost estimate
    @GetMapping("/repairs/{id}/estimate")
    public ResponseEntity<Map<String, Object>> getRepairEstimate(
            @PathVariable Long id,
            Authentication authentication) {
        Map<String, Object> estimate = customerService.getRepairEstimate(id, authentication);
        return ResponseEntity.ok(estimate);
    }

    // Approve cost estimate
    @PostMapping("/repairs/{id}/approve")
    public ResponseEntity<RepairResponseDto> approveEstimate(
            @PathVariable Long id,
            Authentication authentication) {
        RepairResponseDto response = customerService.approveEstimate(id, authentication);
        return ResponseEntity.ok(response);
    }

    // Reject cost estimate
    @PostMapping("/repairs/{id}/reject")
    public ResponseEntity<RepairResponseDto> rejectEstimate(
            @PathVariable Long id,
            Authentication authentication) {
        RepairResponseDto response = customerService.rejectEstimate(id, authentication);
        return ResponseEntity.ok(response);
    }

    // Get final cost
    @GetMapping("/repairs/{id}/final-cost")
    public ResponseEntity<Map<String, Object>> getFinalCost(
            @PathVariable Long id,
            Authentication authentication) {
        Map<String, Object> finalCost = customerService.getFinalCost(id, authentication);
        return ResponseEntity.ok(finalCost);
    }

    // ===== PAYMENT =====

    // ...existing payment endpoints (to be implemented later)...

    // ===== FEEDBACK =====

    // ...existing feedback endpoints (to be implemented later)...
}
