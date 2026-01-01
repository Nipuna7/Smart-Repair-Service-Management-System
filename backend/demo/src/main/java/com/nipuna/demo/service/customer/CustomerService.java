package com.nipuna.demo.service.customer;

import com.nipuna.demo.dto.*;
import com.nipuna.demo.entity.User;
import com.nipuna.demo.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CustomerService {

    private final VehicleRepository vehicleRepository;
    private final RepairRepository repairRepository;
    private final PaymentRepository paymentRepository;
    private final FeedbackRepository feedbackRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    // ===== PROFILE MANAGEMENT =====

    @Transactional(readOnly = true)
    public CustomerProfileDto getProfile(Authentication authentication) {
        User customer = getAuthenticatedCustomer(authentication);
        return mapToProfileDto(customer);
    }

    @Transactional
    public CustomerProfileDto updateProfile(UpdateProfileDto updateDto, Authentication authentication) {
        User customer = getAuthenticatedCustomer(authentication);

        // Validate email uniqueness if changed
        if (updateDto.getEmail() != null && !updateDto.getEmail().equals(customer.getEmail())) {
            if (userRepository.existsByEmail(updateDto.getEmail())) {
                throw new IllegalArgumentException("Email already in use by another account");
            }
            customer.setEmail(updateDto.getEmail());
        }

        // Update profile fields
        if (updateDto.getFullName() != null && !updateDto.getFullName().trim().isEmpty()) {
            customer.setFullName(updateDto.getFullName());
        }

        if (updateDto.getPhone() != null) {
            customer.setPhoneNumber(updateDto.getPhone());
        }

        if (updateDto.getAddress() != null) {
            customer.setAddress(updateDto.getAddress());
        }

        User updated = userRepository.save(customer);
        return mapToProfileDto(updated);
    }

    @Transactional
    public void changePassword(ChangePasswordDto passwordDto, Authentication authentication) {
        User customer = getAuthenticatedCustomer(authentication);

        // Validate current password
        if (!passwordEncoder.matches(passwordDto.getCurrentPassword(), customer.getPassword())) {
            throw new IllegalArgumentException("Current password is incorrect");
        }

        // Validate new password confirmation
        if (!passwordDto.getNewPassword().equals(passwordDto.getConfirmPassword())) {
            throw new IllegalArgumentException("New password and confirmation do not match");
        }

        // Validate new password strength
        if (passwordDto.getNewPassword().length() < 6) {
            throw new IllegalArgumentException("New password must be at least 6 characters long");
        }

        // Ensure new password is different from current
        if (passwordEncoder.matches(passwordDto.getNewPassword(), customer.getPassword())) {
            throw new IllegalArgumentException("New password must be different from current password");
        }

        // Update password
        customer.setPassword(passwordEncoder.encode(passwordDto.getNewPassword()));
        userRepository.save(customer);
    }

    // ...existing code for vehicle management...

    // ...existing code for repair management...

    // ...existing code for payment management...

    // ...existing code for history...

    // ...existing code for feedback...

    // ===== HELPER METHODS =====

    private User getAuthenticatedCustomer(Authentication authentication) {
        String username = authentication.getName();
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));
    }

    private CustomerProfileDto mapToProfileDto(User user) {
        return new CustomerProfileDto(
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                user.getFullName(),
                user.getPhoneNumber(),
                user.getAddress()
        );
    }

    // ...existing helper methods...
}

