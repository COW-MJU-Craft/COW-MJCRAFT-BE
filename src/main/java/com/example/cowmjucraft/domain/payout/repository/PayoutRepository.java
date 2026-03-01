package com.example.cowmjucraft.domain.payout.repository;

import com.example.cowmjucraft.domain.payout.entity.Payout;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PayoutRepository extends JpaRepository<Payout, Long> {

    Optional<Payout> findByProjectId(Long projectId);

    boolean existsByProjectId(Long projectId);
}
