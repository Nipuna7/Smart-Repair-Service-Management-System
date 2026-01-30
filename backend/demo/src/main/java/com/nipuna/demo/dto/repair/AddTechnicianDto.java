package com.nipuna.demo.dto.repair;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;

// DTO for adding a new technician
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AddTechnicianDto {
    // Username for the technician
    @NotBlank(message = "Username is required")
    private String username;

    // Password for the technician
    @NotBlank(message = "Password is required")
    private String password;

    // Email address
    @NotBlank(message = "Email is required")
    @Email(message = "Email should be valid")
    private String email;

    // Full name
    @NotBlank(message = "Full name is required")
    private String fullName;

    // Phone number
    private String phoneNumber;

    // Address
    private String address;

    // Skills (e.g., engine, electrical, bodywork)
    private Set<String> skills;
}

