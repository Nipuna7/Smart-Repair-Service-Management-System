package com.nipuna.demo.controller.repair;

import com.nipuna.demo.dto.repair.RepairRequestDto;
import com.nipuna.demo.dto.repair.RepairResponseDto;
import com.nipuna.demo.dto.MessageResponse;
import com.nipuna.demo.security.UserPrincipal;
import com.nipuna.demo.service.repair.RepairRequestService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/repairs")
@RequiredArgsConstructor
public class RepairRequestController {

    private final RepairRequestService repairRequestService;

    // ENDPOINT 1: Validate vehicle ownership
    // GET /api/repairs/validate-ownership/{vehicleId}
    // Accessible by: CUSTOMER
    @GetMapping("/validate-ownership/{vehicleId}")
    @PreAuthorize("hasAuthority('CUSTOMER')")
    public ResponseEntity<MessageResponse> validateVehicleOwnership(
            @PathVariable Long vehicleId,
            @AuthenticationPrincipal UserPrincipal userPrincipal) {

        // Get authenticated customer ID
        Long customerId = userPrincipal.getId();

        // Call service to validate ownership
        boolean isValid = repairRequestService.validateVehicleOwnership(vehicleId, customerId);

        // Return success message if validation passes
        return ResponseEntity.ok(new MessageResponse("Vehicle ownership validated successfully"));
    }

    // ENDPOINT 2: Create repair request
    // POST /api/repairs
    // Accessible by: CUSTOMER
    @PostMapping
    @PreAuthorize("hasAuthority('CUSTOMER')")
    public ResponseEntity<RepairResponseDto> createRepairRequest(
            @Valid @RequestBody RepairRequestDto requestDto,
            @AuthenticationPrincipal UserPrincipal userPrincipal) {

        // Get authenticated customer ID
        Long customerId = userPrincipal.getId();

        // Call service to create repair request (includes ownership validation)
        RepairResponseDto response = repairRequestService.createRepairRequest(requestDto, customerId);

        // Return created repair with HTTP 201 status
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    // ENDPOINT 3: Check if vehicle has an active repair (Business Logic 1)
    // GET /api/repairs/vehicle/{vehicleId}/check-active
    @GetMapping("/vehicle/{vehicleId}/check-active")
    @PreAuthorize("hasAuthority('CUSTOMER')")
    public ResponseEntity<MessageResponse> checkActiveRepair(
            @PathVariable Long vehicleId,
            @AuthenticationPrincipal UserPrincipal userPrincipal) {

        // Validate ownership first
        repairRequestService.validateVehicleOwnership(vehicleId, userPrincipal.getId());

        // Check for active repairs (will throw if exists)
        repairRequestService.preventMultipleActiveRepairRequests(vehicleId);

        return ResponseEntity.ok(new MessageResponse("No active repair exists for this vehicle"));
    }

    // ENDPOINT 4: Validate repair request input (Business Logic 2)
    // POST /api/repairs/validate-input
    @PostMapping("/validate-input")
    @PreAuthorize("hasAuthority('CUSTOMER')")
    public ResponseEntity<MessageResponse> validateInput(@Valid @RequestBody RepairRequestDto requestDto) {

        // Validate input data
        repairRequestService.validateRepairRequestInput(requestDto);

        return ResponseEntity.ok(new MessageResponse("Repair request input is valid"));
    }

    // ENDPOINT 5: Generate a repair request number (Business Logic 3)
    // GET /api/repairs/generate-request-number
    @GetMapping("/generate-request-number")
    @PreAuthorize("hasAuthority('CUSTOMER')")
    public ResponseEntity<MessageResponse> generateRequestNumber() {

        // Generate unique request number
        String requestNumber = repairRequestService.generateRepairRequestNumber();

        return ResponseEntity.ok(new MessageResponse(requestNumber));
    }

    // ENDPOINT 6: Get recommended priority for a service type (Business Logic 5)
    // GET /api/repairs/assign-priority/{serviceType}
    @GetMapping("/assign-priority/{serviceType}")
    @PreAuthorize("hasAuthority('CUSTOMER')")
    public ResponseEntity<MessageResponse> getRecommendedPriority(@PathVariable String serviceType) {

        // Convert string to enum
        com.nipuna.demo.entity.Repair.ServiceType type;
        try {
            type = com.nipuna.demo.entity.Repair.ServiceType.valueOf(serviceType.toUpperCase());
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                    .body(new MessageResponse("Invalid service type: " + serviceType));
        }

        // Get recommended priority based on service type
        com.nipuna.demo.entity.Repair.RepairPriority priority =
                repairRequestService.assignPriorityByServiceType(type);

        return ResponseEntity.ok(new MessageResponse(
                "Recommended priority for " + serviceType + " is: " + priority.name()));
    }

    // ENDPOINT 7: Test initial status setup (Business Logic 4)
    // GET /api/repairs/test-initial-status
    @GetMapping("/test-initial-status")
    @PreAuthorize("hasAuthority('CUSTOMER')")
    public ResponseEntity<MessageResponse> testInitialStatus() {

        // This endpoint demonstrates what status is set when creating a repair
        return ResponseEntity.ok(new MessageResponse(
                "Initial repair status will be set to: REQUESTED, Payment status: PENDING"));
    }

    // ENDPOINT 8: Get all repair requests for the authenticated customer (Business Logic 8)
    // GET /api/repairs/my-repairs
    @GetMapping("/my-repairs")
    @PreAuthorize("hasAuthority('CUSTOMER')")
    public ResponseEntity<java.util.List<RepairResponseDto>> getMyRepairRequests(
            @AuthenticationPrincipal UserPrincipal userPrincipal) {

        // Get authenticated customer ID
        Long customerId = userPrincipal.getId();

        // Get all repair requests for this customer
        java.util.List<RepairResponseDto> repairs = repairRequestService.getCustomerRepairRequests(customerId);

        return ResponseEntity.ok(repairs);
    }

    // ENDPOINT 9: Approve or reject cost estimate (Business Logic 9)
    // PUT /api/repairs/{repairId}/approve-estimate
    @PutMapping("/{repairId}/approve-estimate")
    @PreAuthorize("hasAuthority('CUSTOMER')")
    public ResponseEntity<RepairResponseDto> approveCostEstimate(
            @PathVariable Long repairId,
            @RequestParam boolean approved,
            @AuthenticationPrincipal UserPrincipal userPrincipal) {

        // Get authenticated customer ID
        Long customerId = userPrincipal.getId();

        // Handle cost estimate approval/rejection
        RepairResponseDto response = repairRequestService.handleCostEstimateApproval(repairId, customerId, approved);

        return ResponseEntity.ok(response);
    }

    // ENDPOINT 10: Cancel repair request (Business Logic 10)
    // DELETE /api/repairs/{repairId}
    @DeleteMapping("/{repairId}")
    @PreAuthorize("hasAuthority('CUSTOMER')")
    public ResponseEntity<RepairResponseDto> cancelRepairRequest(
            @PathVariable Long repairId,
            @RequestParam String cancellationReason,
            @AuthenticationPrincipal UserPrincipal userPrincipal) {

        // Get authenticated customer ID
        Long customerId = userPrincipal.getId();

        // Cancel repair request (only allowed for REQUESTED or ASSIGNED status)
        RepairResponseDto response = repairRequestService.cancelRepairRequest(repairId, customerId, cancellationReason);

        return ResponseEntity.ok(response);
    }

    // ENDPOINT 11: Get complete repair history for authenticated customer (Business Logic 12A)
    // GET /api/repairs/history
    @GetMapping("/history")
    @PreAuthorize("hasAuthority('CUSTOMER')")
    public ResponseEntity<java.util.List<RepairResponseDto>> getMyRepairHistory(
            @AuthenticationPrincipal UserPrincipal userPrincipal) {

        // Get authenticated customer ID
        Long customerId = userPrincipal.getId();

        // Get complete repair history for this customer
        java.util.List<RepairResponseDto> repairs = repairRequestService.getRepairHistoryByCustomer(customerId);

        return ResponseEntity.ok(repairs);
    }

    // ENDPOINT 12: Get repair history for a specific vehicle (Business Logic 12B)
    // GET /api/repairs/vehicle/{vehicleId}/history
    @GetMapping("/vehicle/{vehicleId}/history")
    @PreAuthorize("hasAuthority('CUSTOMER')")
    public ResponseEntity<java.util.List<RepairResponseDto>> getVehicleRepairHistory(
            @PathVariable Long vehicleId,
            @AuthenticationPrincipal UserPrincipal userPrincipal) {

        // Get authenticated customer ID
        Long customerId = userPrincipal.getId();

        // Get repair history for specific vehicle (with ownership validation)
        java.util.List<RepairResponseDto> repairs = repairRequestService.getRepairHistoryByVehicle(vehicleId, customerId);

        return ResponseEntity.ok(repairs);
    }
}

