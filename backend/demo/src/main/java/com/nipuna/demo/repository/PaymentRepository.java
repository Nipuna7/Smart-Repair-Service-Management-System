package com.nipuna.demo.repository;

import com.nipuna.demo.entity.Payment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, Long> {
    Optional<Payment> findByRepairId(Long repairId);
    boolean existsByRepairId(Long repairId);
}

