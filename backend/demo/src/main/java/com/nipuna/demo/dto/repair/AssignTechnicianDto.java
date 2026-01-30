package com.nipuna.demo.dto.repair;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

// DTO for assigning technician to repair request
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AssignTechnicianDto {
    // Technician ID to assign to the repair
    private Long technicianId;
}

