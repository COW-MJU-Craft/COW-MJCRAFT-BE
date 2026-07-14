package com.example.cowmjucraft.domain.order.repository;

import com.example.cowmjucraft.domain.order.entity.MailOutbox;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface MailOutboxRepository extends JpaRepository<MailOutbox, Long> {

    @Query(value = """
            SELECT *
            FROM mail_outboxes
            WHERE (status = 'PENDING' AND next_attempt_at <= :now)
               OR (status = 'PROCESSING' AND claimed_at <= :staleBefore)
            ORDER BY id
            LIMIT :batchSize
            FOR UPDATE SKIP LOCKED
            """, nativeQuery = true)
    List<MailOutbox> findReadyForUpdate(
            @Param("now") LocalDateTime now,
            @Param("staleBefore") LocalDateTime staleBefore,
            @Param("batchSize") int batchSize
    );
}
