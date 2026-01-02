package com.nipuna.demo.controller.customer;

import com.nipuna.demo.service.customer.CustomerService;
import com.nipuna.demo.dto.ChangePasswordDto;
import com.nipuna.demo.dto.customer.CustomerProfileDto;
import com.nipuna.demo.dto.customer.UpdateProfileDto;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/customer")
@CrossOrigin(origins = "*", maxAge = 3600)
@PreAuthorize("hasRole('CUSTOMER')")
@RequiredArgsConstructor
public class CustomerController {

    private final CustomerService customerService;

    // ===== DASHBOARD =====

    @GetMapping("/dashboard")
    public ResponseEntity<?> getDashboard(Authentication authentication) {
        Map<String, Object> response = new HashMap<>();
        response.put("message", "Welcome to Customer Dashboard");
        response.put("user", authentication.getName());
        response.put("authorities", authentication.getAuthorities());
        return ResponseEntity.ok(response);
    }

    // ===== PROFILE MANAGEMENT =====

    @GetMapping("/profile")
    public ResponseEntity<CustomerProfileDto> getProfile(Authentication authentication) {
        CustomerProfileDto profile = customerService.getProfile(authentication);
        return ResponseEntity.ok(profile);
    }

    @PutMapping("/profile")
    public ResponseEntity<CustomerProfileDto> updateProfile(
            @RequestBody UpdateProfileDto updateDto,
            Authentication authentication) {
        CustomerProfileDto updated = customerService.updateProfile(updateDto, authentication);
        return ResponseEntity.ok(updated);
    }

    @PutMapping("/profile/change-password")
    public ResponseEntity<Map<String, String>> changePassword(
            @RequestBody ChangePasswordDto passwordDto,
            Authentication authentication) {
        customerService.changePassword(passwordDto, authentication);
        Map<String, String> response = new HashMap<>();
        response.put("message", "Password changed successfully");
        return ResponseEntity.ok(response);
    }

    // ===== VEHICLE MANAGEMENT =====

    // ...existing vehicle endpoints...

    // ===== REPAIR MANAGEMENT =====

    // ...existing repair endpoints...

    // ===== COST ESTIMATE APPROVAL =====

    // ...existing estimate endpoints...

    // ===== CANCELLATION =====

    // ...existing cancellation endpoints...

    // ===== PAYMENT =====

    // ...existing payment endpoints...

    // ===== HISTORY =====

    // ...existing history endpoints...

    // ===== FEEDBACK =====

    // ...existing feedback endpoints...
}
