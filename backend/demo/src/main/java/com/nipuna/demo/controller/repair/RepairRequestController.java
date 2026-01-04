package com.nipuna.demo.controller.repair;

import com.nipuna.demo.dto.repair.RepairRequestDto;
import com.nipuna.demo.dto.RepairResponseDto;
import com.nipuna.demo.service.repair.RepairRequestService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

@RestController
@RequestMapping("/api/repairs")
@RequiredArgsConstructor
@PreAuthorize("hasRole('CUSTOMER')")
public class RepairRequestController {

    private final RepairRequestService repairRequestService;

    /**
     * Create a new repair request
     * Endpoint: POST /api/repairs
     *
     * Business Logic (delegated to service):
     * 1. Create repair request for customer's vehicle
     * 2. Validate vehicle ownership
     */
    @PostMapping
    public ResponseEntity<?> createRepairRequest(@Valid @RequestBody RepairRequestDto requestDto) {
        try {
            RepairResponseDto response = repairRequestService.createRepairRequest(requestDto);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ErrorResponse(e.getMessage()));
        }
    }

    /**
     * Error response wrapper
     */
    private static class ErrorResponse {
        private String message;

        public ErrorResponse(String message) {
            this.message = message;
        }

        public String getMessage() {
            return message;
        }

        public void setMessage(String message) {
            this.message = message;
        }
    }
}
