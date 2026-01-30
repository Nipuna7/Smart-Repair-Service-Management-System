package com.nipuna.demo.repository;

import com.nipuna.demo.entity.Role;
import com.nipuna.demo.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByUsername(String username);

    Optional<User> findByEmail(String email);

    Boolean existsByUsername(String username);

    Boolean existsByEmail(String email);

    // Find users by role
    @Query("SELECT u FROM User u JOIN u.roles r WHERE r.name = :roleName")
    List<User> findByRoleName(Role.RoleName roleName);

    // Find technicians only
    @Query("SELECT u FROM User u JOIN u.roles r WHERE r.name = 'TECHNICIAN'")
    List<User> findAllTechnicians();
}
