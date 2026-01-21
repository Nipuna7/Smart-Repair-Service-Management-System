package com.nipuna.demo.controller.admin;

import com.nipuna.demo.dto.repair.RepairResponseDto;
import com.nipuna.demo.entity.Repair;
import com.nipuna.demo.service.admin.AdminService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/admin")
@CrossOrigin(origins = "*", maxAge = 3600)
@RequiredArgsConstructor
public class AdminController {

    private final AdminService adminService;

    @GetMapping("/dashboard")
    public ResponseEntity<?> getDashboard(Authentication authentication) {
        Map<String, Object> response = new HashMap<>();
        response.put("message", "Welcome to Admin Dashboard");
        response.put("user", authentication.getName());
        response.put("authorities", authentication.getAuthorities());
        return ResponseEntity.ok(response);
    }

    @GetMapping("/users")
    public ResponseEntity<?> getUsers(Authentication authentication) {
        Map<String, Object> response = new HashMap<>();
        response.put("message", "All Users Management");
        response.put("user", authentication.getName());
        return ResponseEntity.ok(response);
    }

    @GetMapping("/settings")
    public ResponseEntity<?> getSettings(Authentication authentication) {
        Map<String, Object> response = new HashMap<>();
        response.put("message", "System Settings");
        response.put("user", authentication.getName());
        return ResponseEntity.ok(response);
    }

    // ========== REPAIR REQUEST REVIEW SERVICE ENDPOINTS ==========

    // View all repair requests
    @GetMapping("/repairs")
    public ResponseEntity<List<RepairResponseDto>> getAllRepairRequests() {
        List<RepairResponseDto> repairs = adminService.getAllRepairRequests();
        return ResponseEntity.ok(repairs);
    }

    // View repair requests filtered by status
    @GetMapping("/repairs/status/{status}")
    public ResponseEntity<List<RepairResponseDto>> getRepairRequestsByStatus(
            @PathVariable Repair.RepairStatus status) {
        List<RepairResponseDto> repairs = adminService.getRepairRequestsByStatus(status);
        return ResponseEntity.ok(repairs);
    }

    // View repair requests filtered by priority
    @GetMapping("/repairs/priority/{priority}")
    public ResponseEntity<List<RepairResponseDto>> getRepairRequestsByPriority(
            @PathVariable Repair.RepairPriority priority) {
        List<RepairResponseDto> repairs = adminService.getRepairRequestsByPriority(priority);
        return ResponseEntity.ok(repairs);
    }

    // View repair requests filtered by date range
    @GetMapping("/repairs/date-range")
    public ResponseEntity<List<RepairResponseDto>> getRepairRequestsByDateRange(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        List<RepairResponseDto> repairs = adminService.getRepairRequestsByDateRange(startDate, endDate);
        return ResponseEntity.ok(repairs);
    }

    // View specific repair request details
    @GetMapping("/repairs/{id}")
    public ResponseEntity<RepairResponseDto> getRepairRequestById(@PathVariable Long id) {
        RepairResponseDto repair = adminService.getRepairRequestById(id);
        return ResponseEntity.ok(repair);
    }

    // View repair requests with multiple filters (status, priority, date range)
    @GetMapping("/repairs/filter")
    public ResponseEntity<List<RepairResponseDto>> getRepairRequestsWithFilters(
            @RequestParam(required = false) Repair.RepairStatus status,
            @RequestParam(required = false) Repair.RepairPriority priority,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        List<RepairResponseDto> repairs = adminService.getRepairRequestsWithFilters(status, priority, startDate, endDate);
        return ResponseEntity.ok(repairs);
    }
}

