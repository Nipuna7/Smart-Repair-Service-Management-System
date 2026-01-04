package com.nipuna.demo.dto.repair;

import com.nipuna.demo.entity.Repair;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

// DTO for technician to submit repair estimate
@Data
@NoArgsConstructor
@AllArgsConstructor
public class RepairEstimateDto {

    // Estimated cost for the repair
    @NotNull(message = "Estimated cost is required")
    @DecimalMin(value = "0.01", message = "Estimated cost must be greater than 0")
    private BigDecimal estimatedCost;

    // Optional: Update priority based on inspection
    private Repair.RepairPriority priority;
}

