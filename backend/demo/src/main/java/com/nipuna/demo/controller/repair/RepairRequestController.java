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
}
