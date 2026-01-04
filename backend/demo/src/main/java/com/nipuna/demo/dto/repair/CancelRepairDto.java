package com.nipuna.demo.dto.repair;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

// DTO for cancelling a repair request
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CancelRepairDto {

    // Reason for cancelling the repair
    @NotBlank(message = "Cancellation reason is required")
    @Size(min = 10, max = 1000, message = "Cancellation reason must be between 10 and 500 characters")
    private String cancellationReason;
}

