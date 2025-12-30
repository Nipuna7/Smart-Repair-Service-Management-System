package com.nipuna.demo.config;

import com.nipuna.demo.entity.Role;
import com.nipuna.demo.repository.RoleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {
    private final RoleRepository roleRepository;

    @Override
    public void run(String... args) throws Exception {
        // Initialize roles if they don't exist
        if (roleRepository.count() == 0) {
            Role customerRole = new Role();
            customerRole.setName(Role.RoleName.CUSTOMER);
            roleRepository.save(customerRole);

            Role technicianRole = new Role();
            technicianRole.setName(Role.RoleName.TECHNICIAN);
            roleRepository.save(technicianRole);

            Role adminRole = new Role();
            adminRole.setName(Role.RoleName.ADMIN);
            roleRepository.save(adminRole);

            System.out.println("✓ Roles initialized successfully: CUSTOMER, TECHNICIAN, ADMIN");
        } else {
            System.out.println("✓ Roles already exist in database");
        }
    }
}

