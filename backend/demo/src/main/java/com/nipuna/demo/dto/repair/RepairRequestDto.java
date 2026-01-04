package com.nipuna.demo.dto.repair;

import com.nipuna.demo.entity.Repair;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class RepairRequestDto {

    @NotNull(message = "Vehicle ID is required")
    private Long vehicleId;

    @NotBlank(message = "Issue description is required")
    private String issueDescription;

    @NotNull(message = "Service type is required")
    private Repair.ServiceType serviceType;
}

