package com.nipuna.demo.dto.repair;

import jakarta.validation.constraints.Email;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;

// DTO for updating technician profile
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UpdateTechnicianDto {
    // Email address (optional)
    @Email(message = "Email should be valid")
    private String email;

    // Full name (optional)
    private String fullName;

    // Phone number (optional)
    private String phoneNumber;

    // Address (optional)
    private String address;

    // Skills (optional) - e.g., engine, electrical, bodywork
    private Set<String> skills;
}

