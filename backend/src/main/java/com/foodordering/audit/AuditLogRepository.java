package com.foodordering.audit;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.UUID;

public interface AuditLogRepository extends JpaRepository<AuditLog, UUID> {

    List<AuditLog> findTop100ByOrderByCreatedAtDesc();

    List<AuditLog> findByActorIdOrderByCreatedAtDesc(UUID actorId);

    List<AuditLog> findByTargetIdOrderByCreatedAtDesc(String targetId);

    List<AuditLog> findByActionOrderByCreatedAtDesc(String action);
}

