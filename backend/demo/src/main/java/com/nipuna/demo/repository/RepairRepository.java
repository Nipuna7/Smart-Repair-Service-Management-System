package com.nipuna.demo.repository;

import com.nipuna.demo.entity.Repair;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RepairRepository extends JpaRepository<Repair, Long> {
    List<Repair> findByCustomerId(Long customerId);
    List<Repair> findByVehicleId(Long vehicleId);
    Optional<Repair> findByIdAndCustomerId(Long id, Long customerId);
    boolean existsByVehicleIdAndStatusIn(Long vehicleId, List<Repair.RepairStatus> statuses);
    List<Repair> findByCustomerIdAndStatusIn(Long customerId, List<Repair.RepairStatus> statuses);
    List<Repair> findByVehicleIdAndStatus(Long vehicleId, Repair.RepairStatus status);
}

