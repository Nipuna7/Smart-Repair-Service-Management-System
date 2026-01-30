package com.nipuna.demo.dto.repair;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;

// DTO for technician information
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TechnicianDto {
    // Technician ID
    private Long id;

    // Technician name
    private String fullName;

    // Technician email
    private String email;

    // Technician phone number
    private String phoneNumber;

    // Technician skills (e.g., engine, electrical, bodywork)
    private Set<String> skills;

    // Technician status (active/inactive)
    private Boolean enabled;

    // Number of active repairs assigned
    private Integer activeRepairsCount;
}

