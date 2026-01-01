package com.nipuna.demo.repository;

import com.nipuna.demo.entity.Vehicle;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface VehicleRepository extends JpaRepository<Vehicle, Long> {
    List<Vehicle> findByCustomerId(Long customerId);
    Optional<Vehicle> findByVehicleNumberAndCustomerId(String vehicleNumber, Long customerId);
    boolean existsByVehicleNumberAndCustomerId(String vehicleNumber, Long customerId);
}

