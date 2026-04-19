package cocha.vive.backend.repository;

import cocha.vive.backend.model.EmailAuditLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface EmailAuditLogRepository extends JpaRepository<EmailAuditLog, UUID> {
}
