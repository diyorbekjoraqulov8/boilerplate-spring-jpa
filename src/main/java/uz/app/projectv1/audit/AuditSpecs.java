package uz.app.projectv1.audit;

import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;
import uz.app.projectv1.audit.entity.AuditLog;

import java.util.ArrayList;
import java.util.List;

final class AuditSpecs {

    private AuditSpecs() {}

    static Specification<AuditLog> filter(AuditEvent eventType, Long actorId) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (eventType != null) predicates.add(cb.equal(root.get("eventType"), eventType));
            if (actorId   != null) predicates.add(cb.equal(root.get("actorId"), actorId));
            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}