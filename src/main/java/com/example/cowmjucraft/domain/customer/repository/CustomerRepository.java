package com.example.cowmjucraft.domain.customer.repository;

import com.example.cowmjucraft.domain.customer.entity.Customer;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CustomerRepository extends JpaRepository<Customer, Long> {

    /** 인자는 반드시 정규화된 이메일이어야 한다. */
    Optional<Customer> findByEmail(String email);
}
