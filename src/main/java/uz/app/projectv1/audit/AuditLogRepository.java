package uz.app.projectv1.audit;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import uz.app.projectv1.audit.entity.AuditLog;

public interface AuditLogRepository extends JpaRepository<AuditLog, Long>,
                                            JpaSpecificationExecutor<AuditLog> {}
