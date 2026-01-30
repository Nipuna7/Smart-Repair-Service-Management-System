package com.nipuna.demo.controller.admin;

import com.nipuna.demo.dto.repair.AddTechnicianDto;
import com.nipuna.demo.dto.repair.AssignTechnicianDto;
import com.nipuna.demo.dto.repair.RepairResponseDto;
import com.nipuna.demo.dto.repair.TechnicianDto;
import com.nipuna.demo.dto.repair.UpdateTechnicianDto;
import com.nipuna.demo.entity.Repair;
import com.nipuna.demo.service.admin.AdminService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

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

    // ========== TECHNICIAN ASSIGNMENT SERVICE ENDPOINTS ==========

    // Get all available technicians
    @GetMapping("/technicians")
    public ResponseEntity<List<TechnicianDto>> getAllTechnicians() {
        List<TechnicianDto> technicians = adminService.getAllTechnicians();
        return ResponseEntity.ok(technicians);
    }

    // Get all technicians with their workload
    @GetMapping("/technicians/workload")
    public ResponseEntity<List<TechnicianDto>> getTechniciansWithWorkload() {
        List<TechnicianDto> technicians = adminService.getTechniciansWithWorkload();
        return ResponseEntity.ok(technicians);
    }

    // Get specific technician workload
    @GetMapping("/technicians/{technicianId}/workload")
    public ResponseEntity<TechnicianDto> getTechnicianWorkload(@PathVariable Long technicianId) {
        TechnicianDto technician = adminService.getTechnicianWorkload(technicianId);
        return ResponseEntity.ok(technician);
    }

    // Assign technician to repair request
    @PostMapping("/repairs/{repairId}/assign")
    public ResponseEntity<RepairResponseDto> assignTechnicianToRepair(
            @PathVariable Long repairId,
            @RequestBody AssignTechnicianDto assignDto) {
        RepairResponseDto repair = adminService.assignTechnicianToRepair(repairId, assignDto.getTechnicianId());
        return ResponseEntity.ok(repair);
    }

    // Reassign technician to repair request
    @PutMapping("/repairs/{repairId}/reassign")
    public ResponseEntity<RepairResponseDto> reassignTechnicianToRepair(
            @PathVariable Long repairId,
            @RequestBody AssignTechnicianDto assignDto) {
        RepairResponseDto repair = adminService.reassignTechnicianToRepair(repairId, assignDto.getTechnicianId());
        return ResponseEntity.ok(repair);
    }

    // ========== TECHNICIAN MANAGEMENT SERVICE ENDPOINTS ==========

    // Add new technician
    @PostMapping("/technicians/add")
    public ResponseEntity<TechnicianDto> addTechnician(@Valid @RequestBody AddTechnicianDto addTechnicianDto) {
        TechnicianDto technician = adminService.addTechnician(addTechnicianDto);
        return ResponseEntity.ok(technician);
    }

    // Update technician profile
    @PutMapping("/technicians/{technicianId}/update")
    public ResponseEntity<TechnicianDto> updateTechnicianProfile(
            @PathVariable Long technicianId,
            @Valid @RequestBody UpdateTechnicianDto updateTechnicianDto) {
        TechnicianDto technician = adminService.updateTechnicianProfile(technicianId, updateTechnicianDto);
        return ResponseEntity.ok(technician);
    }

    // Assign skills to technician
    @PutMapping("/technicians/{technicianId}/skills")
    public ResponseEntity<TechnicianDto> assignSkillsToTechnician(
            @PathVariable Long technicianId,
            @RequestBody Map<String, Set<String>> requestBody) {
        Set<String> skills = requestBody.get("skills");
        TechnicianDto technician = adminService.assignSkillsToTechnician(technicianId, skills);
        return ResponseEntity.ok(technician);
    }

    // Activate technician
    @PutMapping("/technicians/{technicianId}/activate")
    public ResponseEntity<TechnicianDto> activateTechnician(@PathVariable Long technicianId) {
        TechnicianDto technician = adminService.activateTechnician(technicianId);
        return ResponseEntity.ok(technician);
    }

    // Deactivate technician
    @PutMapping("/technicians/{technicianId}/deactivate")
    public ResponseEntity<TechnicianDto> deactivateTechnician(@PathVariable Long technicianId) {
        TechnicianDto technician = adminService.deactivateTechnician(technicianId);
        return ResponseEntity.ok(technician);
    }

    // Get technician profile by ID
    @GetMapping("/technicians/{technicianId}")
    public ResponseEntity<TechnicianDto> getTechnicianProfile(@PathVariable Long technicianId) {
        TechnicianDto technician = adminService.getTechnicianProfile(technicianId);
        return ResponseEntity.ok(technician);
    }

    // ========== REPAIR MONITORING & CONTROL SERVICE ENDPOINTS ==========

    // Get all active repairs (not completed, cancelled, or delivered)
    @GetMapping("/repairs/active")
    public ResponseEntity<List<RepairResponseDto>> getAllActiveRepairs() {
        List<RepairResponseDto> repairs = adminService.getAllActiveRepairs();
        return ResponseEntity.ok(repairs);
    }

    // Get repairs by specific status
    @GetMapping("/repairs/by-status/{status}")
    public ResponseEntity<List<RepairResponseDto>> getRepairsByStatus(@PathVariable com.nipuna.demo.entity.Repair.RepairStatus status) {
        List<RepairResponseDto> repairs = adminService.getRepairsByStatus(status);
        return ResponseEntity.ok(repairs);
    }

    // Get delayed repairs (exceeding threshold days)
    @GetMapping("/repairs/delayed")
    public ResponseEntity<List<RepairResponseDto>> getDelayedRepairs(
            @RequestParam(defaultValue = "7") int daysThreshold) {
        List<RepairResponseDto> repairs = adminService.getDelayedRepairs(daysThreshold);
        return ResponseEntity.ok(repairs);
    }

    // Get repairs with SLA breaches
    @GetMapping("/repairs/sla-breach")
    public ResponseEntity<List<RepairResponseDto>> getSlaBreach() {
        List<RepairResponseDto> repairs = adminService.getSlaBreach();
        return ResponseEntity.ok(repairs);
    }

    // Get all repairs assigned to a specific technician
    @GetMapping("/repairs/technician/{technicianId}")
    public ResponseEntity<List<RepairResponseDto>> getRepairsByTechnician(@PathVariable Long technicianId) {
        List<RepairResponseDto> repairs = adminService.getRepairsByTechnician(technicianId);
        return ResponseEntity.ok(repairs);
    }

    // Override repair status (admin intervention)
    @PutMapping("/repairs/{repairId}/override-status")
    public ResponseEntity<RepairResponseDto> overrideRepairStatus(
            @PathVariable Long repairId,
            @RequestParam com.nipuna.demo.entity.Repair.RepairStatus newStatus,
            @RequestParam String reason) {
        RepairResponseDto repair = adminService.overrideRepairStatus(repairId, newStatus, reason);
        return ResponseEntity.ok(repair);
    }

    // Force complete repair (exceptional case)
    @PutMapping("/repairs/{repairId}/force-complete")
    public ResponseEntity<RepairResponseDto> forceCompleteRepair(
            @PathVariable Long repairId,
            @RequestParam String reason) {
        RepairResponseDto repair = adminService.forceCompleteRepair(repairId, reason);
        return ResponseEntity.ok(repair);
    }

    // Admin cancel repair (on behalf of customer)
    @PutMapping("/repairs/{repairId}/admin-cancel")
    public ResponseEntity<RepairResponseDto> adminCancelRepair(
            @PathVariable Long repairId,
            @RequestParam String reason) {
        RepairResponseDto repair = adminService.adminCancelRepair(repairId, reason);
        return ResponseEntity.ok(repair);
    }

    // Get repair statistics for dashboard
    @GetMapping("/repairs/statistics")
    public ResponseEntity<java.util.Map<String, Object>> getRepairStatistics() {
        java.util.Map<String, Object> statistics = adminService.getRepairStatistics();
        return ResponseEntity.ok(statistics);
    }
}
