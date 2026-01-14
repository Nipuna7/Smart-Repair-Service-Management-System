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

    // ===== LEGACY ENDPOINT (Keep for compatibility) =====

    // Get assigned tasks (alias for assigned repairs)
    @GetMapping("/tasks")
    public ResponseEntity<List<RepairResponseDto>> getTasks(Authentication authentication) {
        List<RepairResponseDto> repairs = technicianService.getAssignedRepairs(authentication);
        return ResponseEntity.ok(repairs);
    }
}

