package com.example.cowmjucraft.domain.accounts.user.repository;

import com.example.cowmjucraft.domain.accounts.user.entity.Member;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface MemberRepository extends JpaRepository<Member, Long> {
    boolean existsByUserId(String userId);
    Optional<Member> findByUserId(String userId);
}
