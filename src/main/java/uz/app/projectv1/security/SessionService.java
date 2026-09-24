package uz.app.projectv1.security;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import uz.app.projectv1.common.exception.NotFoundException;
import uz.app.projectv1.security.entity.Session;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class SessionService {

    private final SessionRepository sessionRepository;

    @Transactional
    public Session create(Long userId, String device, String ip, Duration ttl, String refreshHash) {
        Session newSession = new Session();

        LocalDateTime now = LocalDateTime.now();

        newSession.setId(UUID.randomUUID());
        newSession.setUserId(userId);
        newSession.setDevice(device);
        newSession.setIp(ip);
        newSession.setCreatedDate(now);
        newSession.setLastSeenAt(now);
        newSession.setExpiresAt(now.plus(ttl));
        newSession.setRefreshTokenHash(refreshHash);

        return this.sessionRepository.save(newSession);
    }

    @Transactional
    public Session rotate(Session session, String newRefreshHash) {
        session.setPreviousRefreshTokenHash(session.getRefreshTokenHash());
        session.setRefreshTokenHash(newRefreshHash);
        return session;
    }

    @Transactional(readOnly = true)
    public boolean isActive(UUID sessionId) {
        return this.sessionRepository.isActive(sessionId, LocalDateTime.now());
    }

    @Transactional
    public void revoke(UUID sessionId) {
        this.sessionRepository.revokeById(sessionId, LocalDateTime.now());
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public int revokeAllForUser(Long userId) {
        return this.sessionRepository.revokeAllByUserId(userId, LocalDateTime.now());
    }

    @Transactional(readOnly = true)
    public List<Session> activeSessions(Long userId) {
        return sessionRepository
                .findAllByUserIdAndRevokedAtIsNullAndExpiresAtAfterOrderByCreatedDateDesc(
                        userId, LocalDateTime.now());
    }

    @Transactional
    public void revokeOwn(Long userId, UUID sessionId) {
        Session session = sessionRepository.findById(sessionId)
                .orElseThrow(() -> new NotFoundException("Sessiya", sessionId));

        if (!session.getUserId().equals(userId)) {
            throw new NotFoundException("Sessiya", sessionId);
        }
        sessionRepository.revokeById(sessionId, LocalDateTime.now());
    }
}