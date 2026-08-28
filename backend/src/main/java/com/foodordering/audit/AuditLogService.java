package com.foodordering.audit;

import com.foodordering.User.entity.User;
import com.foodordering.User.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
public class AuditLogService {

    private static final Logger log = LoggerFactory.getLogger(AuditLogService.class);

    private final AuditLogRepository auditLogRepository;
    private final UserRepository userRepository;

    public AuditLogService(
            AuditLogRepository auditLogRepository,
            UserRepository userRepository
    ) {
        this.auditLogRepository = auditLogRepository;
        this.userRepository = userRepository;
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public AuditLog logAction(String action, String targetId, String targetType, String details) {
        UUID actorId = null;
        String actorEmail = "SYSTEM";
        String actorRole = "SYSTEM";

        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth != null && auth.isAuthenticated() && !"anonymousUser".equals(auth.getPrincipal())) {
                actorEmail = auth.getName();
                User user = userRepository.findByEmail(actorEmail).orElse(null);
                if (user != null) {
                    actorId = user.getId();
                    actorRole = user.getRole();
                }
            }
        } catch (Exception ignored) {
        }

        AuditLog auditLog = new AuditLog(
                action,
                actorId,
                actorEmail,
                actorRole,
                targetId,
                targetType,
                details
        );

        AuditLog saved = auditLogRepository.save(auditLog);
        log.info("AUDIT LOG [{}]: Actor: {} ({}), Target: {} ({}), Details: {}",
                action, actorEmail, actorRole, targetId, targetType, details);
        return saved;
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public AuditLog logActionWithActor(
            String action,
            UUID actorId,
            String actorEmail,
            String actorRole,
            String targetId,
            String targetType,
            String details
    ) {
        AuditLog auditLog = new AuditLog(
                action,
                actorId,
                actorEmail,
                actorRole,
                targetId,
                targetType,
                details
        );

        AuditLog saved = auditLogRepository.save(auditLog);
        log.info("AUDIT LOG [{}]: Actor: {} ({}), Target: {} ({}), Details: {}",
                action, actorEmail, actorRole, targetId, targetType, details);
        return saved;
    }

    @Transactional(readOnly = true)
    public List<AuditLog> getRecentLogs() {
        return auditLogRepository.findTop100ByOrderByCreatedAtDesc();
    }
}

