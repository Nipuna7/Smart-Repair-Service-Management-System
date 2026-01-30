package com.nipuna.demo.service.admin;

import com.nipuna.demo.dto.repair.AddTechnicianDto;
import com.nipuna.demo.dto.repair.RepairResponseDto;
import com.nipuna.demo.dto.repair.TechnicianDto;
import com.nipuna.demo.dto.repair.UpdateTechnicianDto;
import com.nipuna.demo.entity.Repair;
import com.nipuna.demo.entity.Role;
import com.nipuna.demo.entity.User;
import com.nipuna.demo.repository.RepairRepository;
import com.nipuna.demo.repository.RoleRepository;
import com.nipuna.demo.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AdminService {

    private final RepairRepository repairRepository;
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    // ========== REPAIR REQUEST REVIEW SERVICE ==========

    // View all repair requests
    @Transactional(readOnly = true)
    public List<RepairResponseDto> getAllRepairRequests() {
        List<Repair> repairs = repairRepository.findAll();
        return repairs.stream()
                .map(this::convertToRepairResponseDto)
                .collect(Collectors.toList());
    }

    // View repair requests filtered by status
    @Transactional(readOnly = true)
    public List<RepairResponseDto> getRepairRequestsByStatus(Repair.RepairStatus status) {
        List<Repair> repairs = repairRepository.findAll();
        return repairs.stream()
                .filter(repair -> repair.getStatus() == status)
                .map(this::convertToRepairResponseDto)
                .collect(Collectors.toList());
    }

    // View repair requests filtered by priority
    @Transactional(readOnly = true)
    public List<RepairResponseDto> getRepairRequestsByPriority(Repair.RepairPriority priority) {
        List<Repair> repairs = repairRepository.findAll();
        return repairs.stream()
                .filter(repair -> repair.getPriority() == priority)
                .map(this::convertToRepairResponseDto)
                .collect(Collectors.toList());
    }

    // View repair requests filtered by date range
    @Transactional(readOnly = true)
    public List<RepairResponseDto> getRepairRequestsByDateRange(LocalDate startDate, LocalDate endDate) {
        List<Repair> repairs = repairRepository.findAll();
        LocalDateTime startDateTime = startDate.atStartOfDay();
        LocalDateTime endDateTime = endDate.atTime(23, 59, 59);

        return repairs.stream()
                .filter(repair -> {
                    LocalDateTime createdAt = repair.getCreatedAt();
                    return createdAt != null &&
                           !createdAt.isBefore(startDateTime) &&
                           !createdAt.isAfter(endDateTime);
                })
                .map(this::convertToRepairResponseDto)
                .collect(Collectors.toList());
    }

    // View specific repair request details by ID
    @Transactional(readOnly = true)
    public RepairResponseDto getRepairRequestById(Long repairId) {
        Repair repair = repairRepository.findById(repairId)
                .orElseThrow(() -> new RuntimeException("Repair request not found with ID: " + repairId));
        return convertToRepairResponseDto(repair);
    }

    // View repair requests filtered by multiple criteria (status, priority, date)
    @Transactional(readOnly = true)
    public List<RepairResponseDto> getRepairRequestsWithFilters(
            Repair.RepairStatus status,
            Repair.RepairPriority priority,
            LocalDate startDate,
            LocalDate endDate) {

        List<Repair> repairs = repairRepository.findAll();

        return repairs.stream()
                .filter(repair -> {
                    // Filter by status if provided
                    if (status != null && repair.getStatus() != status) {
                        return false;
                    }

                    // Filter by priority if provided
                    if (priority != null && repair.getPriority() != priority) {
                        return false;
                    }

                    // Filter by date range if provided
                    if (startDate != null && endDate != null) {
                        LocalDateTime createdAt = repair.getCreatedAt();
                        LocalDateTime startDateTime = startDate.atStartOfDay();
                        LocalDateTime endDateTime = endDate.atTime(23, 59, 59);

                        if (createdAt == null ||
                            createdAt.isBefore(startDateTime) ||
                            createdAt.isAfter(endDateTime)) {
                            return false;
                        }
                    }

                    return true;
                })
                .map(this::convertToRepairResponseDto)
                .collect(Collectors.toList());
    }

    // ========== TECHNICIAN ASSIGNMENT SERVICE ==========

    // Get all available technicians
    @Transactional(readOnly = true)
    public List<TechnicianDto> getAllTechnicians() {
        List<User> technicians = userRepository.findAllTechnicians();
        return technicians.stream()
                .map(this::convertToTechnicianDto)
                .collect(Collectors.toList());
    }

    // Assign technician to repair request
    @Transactional
    public RepairResponseDto assignTechnicianToRepair(Long repairId, Long technicianId) {
        // Find repair by ID
        Repair repair = repairRepository.findById(repairId)
                .orElseThrow(() -> new RuntimeException("Repair request not found with ID: " + repairId));

        // Business rule: Ensure repair status is REQUESTED before assignment
        if (repair.getStatus() != Repair.RepairStatus.REQUESTED) {
            throw new RuntimeException("Cannot assign technician. Repair status must be REQUESTED. Current status: " + repair.getStatus());
        }

        // Find technician by ID
        User technician = userRepository.findById(technicianId)
                .orElseThrow(() -> new RuntimeException("Technician not found with ID: " + technicianId));

        // Verify user is actually a technician
        boolean isTechnician = technician.getRoles().stream()
                .anyMatch(role -> role.getName() == Role.RoleName.TECHNICIAN);

        if (!isTechnician) {
            throw new RuntimeException("User with ID " + technicianId + " is not a technician");
        }

        // Assign technician to repair
        repair.setTechnician(technician);
        repair.setStatus(Repair.RepairStatus.ASSIGNED);
        repair.setAssignedAt(LocalDateTime.now());

        // Save repair
        Repair savedRepair = repairRepository.save(repair);

        return convertToRepairResponseDto(savedRepair);
    }

    // Reassign technician to repair request
    @Transactional
    public RepairResponseDto reassignTechnicianToRepair(Long repairId, Long newTechnicianId) {
        // Find repair by ID
        Repair repair = repairRepository.findById(repairId)
                .orElseThrow(() -> new RuntimeException("Repair request not found with ID: " + repairId));

        // Business rule: Cannot reassign completed or cancelled repairs
        if (repair.getStatus() == Repair.RepairStatus.COMPLETED ||
            repair.getStatus() == Repair.RepairStatus.CANCELLED ||
            repair.getStatus() == Repair.RepairStatus.DELIVERED) {
            throw new RuntimeException("Cannot reassign technician. Repair is already " + repair.getStatus());
        }

        // Find new technician by ID
        User newTechnician = userRepository.findById(newTechnicianId)
                .orElseThrow(() -> new RuntimeException("Technician not found with ID: " + newTechnicianId));

        // Verify user is actually a technician
        boolean isTechnician = newTechnician.getRoles().stream()
                .anyMatch(role -> role.getName() == Role.RoleName.TECHNICIAN);

        if (!isTechnician) {
            throw new RuntimeException("User with ID " + newTechnicianId + " is not a technician");
        }

        // Check if trying to reassign to the same technician
        if (repair.getTechnician() != null && repair.getTechnician().getId().equals(newTechnicianId)) {
            throw new RuntimeException("Repair is already assigned to this technician");
        }

        // Reassign technician
        repair.setTechnician(newTechnician);

        // Update status to ASSIGNED if it was REQUESTED
        if (repair.getStatus() == Repair.RepairStatus.REQUESTED) {
            repair.setStatus(Repair.RepairStatus.ASSIGNED);
            repair.setAssignedAt(LocalDateTime.now());
        }

        // Save repair
        Repair savedRepair = repairRepository.save(repair);

        return convertToRepairResponseDto(savedRepair);
    }

    // Get technician workload (count of active repairs)
    @Transactional(readOnly = true)
    public TechnicianDto getTechnicianWorkload(Long technicianId) {
        // Find technician by ID
        User technician = userRepository.findById(technicianId)
                .orElseThrow(() -> new RuntimeException("Technician not found with ID: " + technicianId));

        // Verify user is actually a technician
        boolean isTechnician = technician.getRoles().stream()
                .anyMatch(role -> role.getName() == Role.RoleName.TECHNICIAN);

        if (!isTechnician) {
            throw new RuntimeException("User with ID " + technicianId + " is not a technician");
        }

        return convertToTechnicianDto(technician);
    }

    // Get all technicians with their workload
    @Transactional(readOnly = true)
    public List<TechnicianDto> getTechniciansWithWorkload() {
        List<User> technicians = userRepository.findAllTechnicians();
        return technicians.stream()
                .map(this::convertToTechnicianDto)
                .collect(Collectors.toList());
    }

    // ========== TECHNICIAN MANAGEMENT SERVICE ==========

    // Add new technician
    @Transactional
    public TechnicianDto addTechnician(AddTechnicianDto addTechnicianDto) {
        // Check if username already exists
        if (userRepository.findByUsername(addTechnicianDto.getUsername()).isPresent()) {
            throw new RuntimeException("Username already exists: " + addTechnicianDto.getUsername());
        }

        // Check if email already exists
        if (userRepository.findByEmail(addTechnicianDto.getEmail()).isPresent()) {
            throw new RuntimeException("Email already exists: " + addTechnicianDto.getEmail());
        }

        // Create new technician user
        User technician = new User();
        technician.setUsername(addTechnicianDto.getUsername());
        technician.setPassword(passwordEncoder.encode(addTechnicianDto.getPassword()));
        technician.setEmail(addTechnicianDto.getEmail());
        technician.setFullName(addTechnicianDto.getFullName());
        technician.setPhoneNumber(addTechnicianDto.getPhoneNumber());
        technician.setAddress(addTechnicianDto.getAddress());
        technician.setEnabled(true);

        // Set skills
        if (addTechnicianDto.getSkills() != null && !addTechnicianDto.getSkills().isEmpty()) {
            technician.setSkills(new HashSet<>(addTechnicianDto.getSkills()));
        }

        // Assign TECHNICIAN role
        Role technicianRole = roleRepository.findByName(Role.RoleName.TECHNICIAN)
                .orElseThrow(() -> new RuntimeException("TECHNICIAN role not found in database"));

        Set<Role> roles = new HashSet<>();
        roles.add(technicianRole);
        technician.setRoles(roles);

        // Save technician
        User savedTechnician = userRepository.save(technician);

        return convertToTechnicianDto(savedTechnician);
    }

    // Update technician profile
    @Transactional
    public TechnicianDto updateTechnicianProfile(Long technicianId, UpdateTechnicianDto updateTechnicianDto) {
        // Find technician by ID
        User technician = userRepository.findById(technicianId)
                .orElseThrow(() -> new RuntimeException("Technician not found with ID: " + technicianId));

        // Verify user is actually a technician
        boolean isTechnician = technician.getRoles().stream()
                .anyMatch(role -> role.getName() == Role.RoleName.TECHNICIAN);

        if (!isTechnician) {
            throw new RuntimeException("User with ID " + technicianId + " is not a technician");
        }

        // Update email if provided and not already taken
        if (updateTechnicianDto.getEmail() != null && !updateTechnicianDto.getEmail().isEmpty()) {
            if (!updateTechnicianDto.getEmail().equals(technician.getEmail())) {
                if (userRepository.findByEmail(updateTechnicianDto.getEmail()).isPresent()) {
                    throw new RuntimeException("Email already exists: " + updateTechnicianDto.getEmail());
                }
                technician.setEmail(updateTechnicianDto.getEmail());
            }
        }

        // Update full name if provided
        if (updateTechnicianDto.getFullName() != null && !updateTechnicianDto.getFullName().isEmpty()) {
            technician.setFullName(updateTechnicianDto.getFullName());
        }

        // Update phone number if provided
        if (updateTechnicianDto.getPhoneNumber() != null) {
            technician.setPhoneNumber(updateTechnicianDto.getPhoneNumber());
        }

        // Update address if provided
        if (updateTechnicianDto.getAddress() != null) {
            technician.setAddress(updateTechnicianDto.getAddress());
        }

        // Update skills if provided
        if (updateTechnicianDto.getSkills() != null) {
            technician.setSkills(new HashSet<>(updateTechnicianDto.getSkills()));
        }

        // Save updated technician
        User updatedTechnician = userRepository.save(technician);

        return convertToTechnicianDto(updatedTechnician);
    }

    // Assign skills to technician
    @Transactional
    public TechnicianDto assignSkillsToTechnician(Long technicianId, Set<String> skills) {
        // Find technician by ID
        User technician = userRepository.findById(technicianId)
                .orElseThrow(() -> new RuntimeException("Technician not found with ID: " + technicianId));

        // Verify user is actually a technician
        boolean isTechnician = technician.getRoles().stream()
                .anyMatch(role -> role.getName() == Role.RoleName.TECHNICIAN);

        if (!isTechnician) {
            throw new RuntimeException("User with ID " + technicianId + " is not a technician");
        }

        // Assign skills
        technician.setSkills(new HashSet<>(skills));

        // Save technician
        User savedTechnician = userRepository.save(technician);

        return convertToTechnicianDto(savedTechnician);
    }

    // Activate technician
    @Transactional
    public TechnicianDto activateTechnician(Long technicianId) {
        // Find technician by ID
        User technician = userRepository.findById(technicianId)
                .orElseThrow(() -> new RuntimeException("Technician not found with ID: " + technicianId));

        // Verify user is actually a technician
        boolean isTechnician = technician.getRoles().stream()
                .anyMatch(role -> role.getName() == Role.RoleName.TECHNICIAN);

        if (!isTechnician) {
            throw new RuntimeException("User with ID " + technicianId + " is not a technician");
        }

        // Activate technician
        technician.setEnabled(true);

        // Save technician
        User savedTechnician = userRepository.save(technician);

        return convertToTechnicianDto(savedTechnician);
    }

    // Deactivate technician
    @Transactional
    public TechnicianDto deactivateTechnician(Long technicianId) {
        // Find technician by ID
        User technician = userRepository.findById(technicianId)
                .orElseThrow(() -> new RuntimeException("Technician not found with ID: " + technicianId));

        // Verify user is actually a technician
        boolean isTechnician = technician.getRoles().stream()
                .anyMatch(role -> role.getName() == Role.RoleName.TECHNICIAN);

        if (!isTechnician) {
            throw new RuntimeException("User with ID " + technicianId + " is not a technician");
        }

        // Check if technician has active repairs
        List<Repair.RepairStatus> activeStatuses = List.of(
                Repair.RepairStatus.ASSIGNED,
                Repair.RepairStatus.IN_PROGRESS,
                Repair.RepairStatus.ESTIMATE_SUBMITTED,
                Repair.RepairStatus.APPROVED
        );

        List<Repair> activeRepairs = repairRepository.findByTechnicianId(technician.getId())
                .stream()
                .filter(repair -> activeStatuses.contains(repair.getStatus()))
                .collect(Collectors.toList());

        if (!activeRepairs.isEmpty()) {
            throw new RuntimeException("Cannot deactivate technician. Technician has " + activeRepairs.size() + " active repair(s)");
        }

        // Deactivate technician
        technician.setEnabled(false);

        // Save technician
        User savedTechnician = userRepository.save(technician);

        return convertToTechnicianDto(savedTechnician);
    }

    // Get technician profile by ID
    @Transactional(readOnly = true)
    public TechnicianDto getTechnicianProfile(Long technicianId) {
        // Find technician by ID
        User technician = userRepository.findById(technicianId)
                .orElseThrow(() -> new RuntimeException("Technician not found with ID: " + technicianId));

        // Verify user is actually a technician
        boolean isTechnician = technician.getRoles().stream()
                .anyMatch(role -> role.getName() == Role.RoleName.TECHNICIAN);

        if (!isTechnician) {
            throw new RuntimeException("User with ID " + technicianId + " is not a technician");
        }

        return convertToTechnicianDto(technician);
    }

    // ========== REPAIR MONITORING & CONTROL SERVICE ==========

    // Monitor all active repairs and get their current status
    @Transactional(readOnly = true)
    public List<RepairResponseDto> getAllActiveRepairs() {
        // Define active statuses (not completed, cancelled, or delivered)
        List<Repair.RepairStatus> activeStatuses = List.of(
                Repair.RepairStatus.REQUESTED,
                Repair.RepairStatus.ASSIGNED,
                Repair.RepairStatus.IN_PROGRESS,
                Repair.RepairStatus.ESTIMATE_SUBMITTED,
                Repair.RepairStatus.APPROVED
        );

        List<Repair> repairs = repairRepository.findAll();
        return repairs.stream()
                .filter(repair -> activeStatuses.contains(repair.getStatus()))
                .map(this::convertToRepairResponseDto)
                .collect(Collectors.toList());
    }

    // Get repairs by specific status for monitoring
    @Transactional(readOnly = true)
    public List<RepairResponseDto> getRepairsByStatus(Repair.RepairStatus status) {
        List<Repair> repairs = repairRepository.findAll();
        return repairs.stream()
                .filter(repair -> repair.getStatus() == status)
                .map(this::convertToRepairResponseDto)
                .collect(Collectors.toList());
    }

    // Detect delayed repairs (repairs that exceed expected completion time)
    @Transactional(readOnly = true)
    public List<RepairResponseDto> getDelayedRepairs(int daysThreshold) {
        // Get all active repairs
        List<Repair.RepairStatus> activeStatuses = List.of(
                Repair.RepairStatus.ASSIGNED,
                Repair.RepairStatus.IN_PROGRESS,
                Repair.RepairStatus.APPROVED
        );

        List<Repair> repairs = repairRepository.findAll();
        LocalDateTime thresholdDateTime = LocalDateTime.now().minusDays(daysThreshold);

        return repairs.stream()
                .filter(repair -> activeStatuses.contains(repair.getStatus()))
                .filter(repair -> {
                    // Check if repair has been in active status for more than threshold days
                    LocalDateTime referenceTime = repair.getAssignedAt() != null
                            ? repair.getAssignedAt()
                            : repair.getCreatedAt();
                    return referenceTime != null && referenceTime.isBefore(thresholdDateTime);
                })
                .map(this::convertToRepairResponseDto)
                .collect(Collectors.toList());
    }

    // Detect repairs with SLA breaches (high priority repairs that are delayed)
    @Transactional(readOnly = true)
    public List<RepairResponseDto> getSlaBreach() {
        // Define SLA thresholds based on priority
        // URGENT priority: 2 days, HIGH: 5 days, NORMAL: 10 days, LOW: 14 days
        List<Repair> repairs = repairRepository.findAll();
        LocalDateTime now = LocalDateTime.now();

        return repairs.stream()
                .filter(repair -> {
                    // Only check active repairs
                    Repair.RepairStatus status = repair.getStatus();
                    if (status == Repair.RepairStatus.COMPLETED ||
                        status == Repair.RepairStatus.CANCELLED ||
                        status == Repair.RepairStatus.DELIVERED) {
                        return false;
                    }

                    // Get reference time (when repair was assigned or created)
                    LocalDateTime referenceTime = repair.getAssignedAt() != null
                            ? repair.getAssignedAt()
                            : repair.getCreatedAt();

                    if (referenceTime == null) {
                        return false;
                    }

                    // Calculate days elapsed
                    long daysElapsed = java.time.Duration.between(referenceTime, now).toDays();

                    // Check SLA based on priority
                    Repair.RepairPriority priority = repair.getPriority();
                    if (priority == Repair.RepairPriority.URGENT && daysElapsed > 2) {
                        return true;
                    } else if (priority == Repair.RepairPriority.HIGH && daysElapsed > 5) {
                        return true;
                    } else if (priority == Repair.RepairPriority.NORMAL && daysElapsed > 10) {
                        return true;
                    } else return priority == Repair.RepairPriority.LOW && daysElapsed > 14;
                })
                .map(this::convertToRepairResponseDto)
                .collect(Collectors.toList());
    }

    // Get repairs by technician for monitoring technician workload
    @Transactional(readOnly = true)
    public List<RepairResponseDto> getRepairsByTechnician(Long technicianId) {
        // Find technician
        User technician = userRepository.findById(technicianId)
                .orElseThrow(() -> new RuntimeException("Technician not found with ID: " + technicianId));

        // Verify user is a technician
        boolean isTechnician = technician.getRoles().stream()
                .anyMatch(role -> role.getName() == Role.RoleName.TECHNICIAN);

        if (!isTechnician) {
            throw new RuntimeException("User with ID " + technicianId + " is not a technician");
        }

        // Get all repairs assigned to this technician
        List<Repair> repairs = repairRepository.findByTechnicianId(technicianId);
        return repairs.stream()
                .map(this::convertToRepairResponseDto)
                .collect(Collectors.toList());
    }

    // Override repair status in exceptional cases (admin intervention)
    @Transactional
    public RepairResponseDto overrideRepairStatus(Long repairId, Repair.RepairStatus newStatus, String reason) {
        // Find repair by ID
        Repair repair = repairRepository.findById(repairId)
                .orElseThrow(() -> new RuntimeException("Repair request not found with ID: " + repairId));

        // Validate reason is provided for override
        if (reason == null || reason.trim().isEmpty()) {
            throw new RuntimeException("Reason is required for status override");
        }

        // Store current status for logging
        Repair.RepairStatus oldStatus = repair.getStatus();

        // Update status
        repair.setStatus(newStatus);

        // Update timestamps based on new status
        LocalDateTime now = LocalDateTime.now();
        switch (newStatus) {
            case ASSIGNED:
                if (repair.getAssignedAt() == null) {
                    repair.setAssignedAt(now);
                }
                break;
            case IN_PROGRESS:
                if (repair.getInProgressAt() == null) {
                    repair.setInProgressAt(now);
                }
                break;
            case COMPLETED:
                if (repair.getCompletedAt() == null) {
                    repair.setCompletedAt(now);
                }
                break;
            case CANCELLED:
                if (repair.getCancelledAt() == null) {
                    repair.setCancelledAt(now);
                }
                repair.setCancellationReason("Admin override: " + reason);
                break;
        }

        // Add reason to repair notes
        String overrideNote = String.format("[ADMIN OVERRIDE] Status changed from %s to %s. Reason: %s (at %s)",
                oldStatus, newStatus, reason, now);

        String currentNotes = repair.getRepairNotes();
        if (currentNotes != null && !currentNotes.isEmpty()) {
            repair.setRepairNotes(currentNotes + "\n" + overrideNote);
        } else {
            repair.setRepairNotes(overrideNote);
        }

        // Save repair
        Repair savedRepair = repairRepository.save(repair);

        return convertToRepairResponseDto(savedRepair);
    }

    // Force complete repair (exceptional case - skip normal workflow)
    @Transactional
    public RepairResponseDto forceCompleteRepair(Long repairId, String reason) {
        // Find repair by ID
        Repair repair = repairRepository.findById(repairId)
                .orElseThrow(() -> new RuntimeException("Repair request not found with ID: " + repairId));

        // Check if already completed
        if (repair.getStatus() == Repair.RepairStatus.COMPLETED) {
            throw new RuntimeException("Repair is already completed");
        }

        // Check if repair is cancelled
        if (repair.getStatus() == Repair.RepairStatus.CANCELLED) {
            throw new RuntimeException("Cannot complete a cancelled repair");
        }

        // Validate reason is provided
        if (reason == null || reason.trim().isEmpty()) {
            throw new RuntimeException("Reason is required for force completion");
        }

        // Force complete the repair
        repair.setStatus(Repair.RepairStatus.COMPLETED);
        repair.setCompletedAt(LocalDateTime.now());

        // Add reason to repair notes
        String forceCompleteNote = String.format("[ADMIN FORCE COMPLETE] Repair force completed. Reason: %s (at %s)",
                reason, LocalDateTime.now());

        String currentNotes = repair.getRepairNotes();
        if (currentNotes != null && !currentNotes.isEmpty()) {
            repair.setRepairNotes(currentNotes + "\n" + forceCompleteNote);
        } else {
            repair.setRepairNotes(forceCompleteNote);
        }

        // Save repair
        Repair savedRepair = repairRepository.save(repair);

        return convertToRepairResponseDto(savedRepair);
    }

    // Cancel repair on behalf of customer (exceptional case)
    @Transactional
    public RepairResponseDto adminCancelRepair(Long repairId, String reason) {
        // Find repair by ID
        Repair repair = repairRepository.findById(repairId)
                .orElseThrow(() -> new RuntimeException("Repair request not found with ID: " + repairId));

        // Check if already completed or delivered
        if (repair.getStatus() == Repair.RepairStatus.COMPLETED ||
            repair.getStatus() == Repair.RepairStatus.DELIVERED) {
            throw new RuntimeException("Cannot cancel repair. Repair is already " + repair.getStatus());
        }

        // Check if already cancelled
        if (repair.getStatus() == Repair.RepairStatus.CANCELLED) {
            throw new RuntimeException("Repair is already cancelled");
        }

        // Validate reason is provided
        if (reason == null || reason.trim().isEmpty()) {
            throw new RuntimeException("Reason is required for admin cancellation");
        }

        // Cancel the repair
        repair.setStatus(Repair.RepairStatus.CANCELLED);
        repair.setCancelledAt(LocalDateTime.now());
        repair.setCancellationReason("Admin cancelled: " + reason);

        // Add reason to repair notes
        String cancelNote = String.format("[ADMIN CANCELLATION] Repair cancelled by admin. Reason: %s (at %s)",
                reason, LocalDateTime.now());

        String currentNotes = repair.getRepairNotes();
        if (currentNotes != null && !currentNotes.isEmpty()) {
            repair.setRepairNotes(currentNotes + "\n" + cancelNote);
        } else {
            repair.setRepairNotes(cancelNote);
        }

        // Save repair
        Repair savedRepair = repairRepository.save(repair);

        return convertToRepairResponseDto(savedRepair);
    }

    // Get repair statistics for monitoring dashboard
    @Transactional(readOnly = true)
    public java.util.Map<String, Object> getRepairStatistics() {
        List<Repair> allRepairs = repairRepository.findAll();

        // Count repairs by status
        long requestedCount = allRepairs.stream().filter(r -> r.getStatus() == Repair.RepairStatus.REQUESTED).count();
        long assignedCount = allRepairs.stream().filter(r -> r.getStatus() == Repair.RepairStatus.ASSIGNED).count();
        long inProgressCount = allRepairs.stream().filter(r -> r.getStatus() == Repair.RepairStatus.IN_PROGRESS).count();
        long completedCount = allRepairs.stream().filter(r -> r.getStatus() == Repair.RepairStatus.COMPLETED).count();
        long cancelledCount = allRepairs.stream().filter(r -> r.getStatus() == Repair.RepairStatus.CANCELLED).count();

        // Count repairs by priority
        long urgentPriorityCount = allRepairs.stream().filter(r -> r.getPriority() == Repair.RepairPriority.URGENT).count();
        long highPriorityCount = allRepairs.stream().filter(r -> r.getPriority() == Repair.RepairPriority.HIGH).count();
        long normalPriorityCount = allRepairs.stream().filter(r -> r.getPriority() == Repair.RepairPriority.NORMAL).count();
        long lowPriorityCount = allRepairs.stream().filter(r -> r.getPriority() == Repair.RepairPriority.LOW).count();

        // Count delayed repairs (using 7 days as default threshold)
        long delayedCount = getDelayedRepairs(7).size();

        // Count SLA breaches
        long slaBreachCount = getSlaBreach().size();

        // Build statistics map
        java.util.Map<String, Object> statistics = new java.util.HashMap<>();
        statistics.put("totalRepairs", allRepairs.size());
        statistics.put("requestedRepairs", requestedCount);
        statistics.put("assignedRepairs", assignedCount);
        statistics.put("inProgressRepairs", inProgressCount);
        statistics.put("completedRepairs", completedCount);
        statistics.put("cancelledRepairs", cancelledCount);
        statistics.put("urgentPriorityRepairs", urgentPriorityCount);
        statistics.put("highPriorityRepairs", highPriorityCount);
        statistics.put("normalPriorityRepairs", normalPriorityCount);
        statistics.put("lowPriorityRepairs", lowPriorityCount);
        statistics.put("delayedRepairs", delayedCount);
        statistics.put("slaBreaches", slaBreachCount);

        return statistics;
    }

    // ========== HELPER METHODS ==========

    // Convert Repair entity to RepairResponseDto
    private RepairResponseDto convertToRepairResponseDto(Repair repair) {
        return RepairResponseDto.builder()
                .id(repair.getId())
                .repairRequestNumber(repair.getRepairRequestNumber())
                .vehicleId(repair.getVehicle().getId())
                .vehicleNumber(repair.getVehicle().getVehicleNumber())
                .vehicleMake(repair.getVehicle().getMake())
                .vehicleModel(repair.getVehicle().getModel())
                .customerId(repair.getCustomer().getId())
                .customerName(repair.getCustomer().getFullName())
                .customerEmail(repair.getCustomer().getEmail())
                .technicianId(repair.getTechnician() != null ? repair.getTechnician().getId() : null)
                .technicianName(repair.getTechnician() != null ? repair.getTechnician().getFullName() : null)
                .serviceType(repair.getServiceType())
                .issueDescription(repair.getIssueDescription())
                .status(repair.getStatus())
                .priority(repair.getPriority())
                .estimatedCost(repair.getEstimatedCost())
                .finalCost(repair.getFinalCost())
                .paymentStatus(repair.getPaymentStatus())
                .estimateApproved(repair.getEstimateApproved())
                .createdAt(repair.getCreatedAt())
                .assignedAt(repair.getAssignedAt())
                .inProgressAt(repair.getInProgressAt())
                .completedAt(repair.getCompletedAt())
                .cancelledAt(repair.getCancelledAt())
                .updatedAt(repair.getUpdatedAt())
                .cancellationReason(repair.getCancellationReason())
                .diagnosisDetails(repair.getDiagnosisDetails())
                .repairNotes(repair.getRepairNotes())
                .build();
    }

    // Convert User entity to TechnicianDto
    private TechnicianDto convertToTechnicianDto(User technician) {
        // Count active repairs for this technician
        List<Repair.RepairStatus> activeStatuses = List.of(
                Repair.RepairStatus.ASSIGNED,
                Repair.RepairStatus.IN_PROGRESS,
                Repair.RepairStatus.ESTIMATE_SUBMITTED,
                Repair.RepairStatus.APPROVED
        );

        List<Repair> activeRepairs = repairRepository.findByTechnicianId(technician.getId())
                .stream()
                .filter(repair -> activeStatuses.contains(repair.getStatus()))
                .collect(Collectors.toList());

        return TechnicianDto.builder()
                .id(technician.getId())
                .fullName(technician.getFullName())
                .email(technician.getEmail())
                .phoneNumber(technician.getPhoneNumber())
                .skills(technician.getSkills())
                .enabled(technician.getEnabled())
                .activeRepairsCount(activeRepairs.size())
                .build();
    }
}
