package com.nipuna.demo.dto.repair;

import com.nipuna.demo.entity.Repair;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for creating a new repair request
 * Used by customers to submit repair requests for their vehicles
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class RepairRequestDto {

    /**
     * ID of the vehicle requiring repair
     * Must be owned by the authenticated customer
     */
    @NotNull(message = "Vehicle ID is required")
    private Long vehicleId;

    /**
     * Type of service requested
     * Examples: BREAKDOWN, REGULAR_SERVICE, INSPECTION, etc.
     */
    @NotNull(message = "Service type is required")
    private Repair.ServiceType serviceType;

    /**
     * Detailed description of the issue or service needed
     */
    @NotBlank(message = "Issue description is required")
    @Size(min = 10, max = 1000, message = "Description must be between 10 and 1000 characters")
    private String issueDescription;

    /**
     * Priority level of the repair
     * Optional - defaults to NORMAL if not specified
     */
    private Repair.RepairPriority priority;
}

