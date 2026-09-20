package uz.app.projectv1.security;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
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
    public Session create(Long userId, String device, String ip, Duration ttl) {
        Session newSession = new Session();

        LocalDateTime now = LocalDateTime.now();

        newSession.setId(UUID.randomUUID());
        newSession.setUserId(userId);
        newSession.setDevice(device);
        newSession.setIp(ip);
        newSession.setCreatedDate(now);
        newSession.setLastSeenAt(now);
        newSession.setExpiresAt(now.plus(ttl));

        return this.sessionRepository.save(newSession);
    }

    @Transactional(readOnly = true)
    public boolean isActive(UUID sessionId) {
        return this.sessionRepository.isActive(sessionId, LocalDateTime.now());
    }

    @Transactional
    public void revoke(UUID sessionId) {
        this.sessionRepository.revokeById(sessionId, LocalDateTime.now());
    }

    @Transactional
    public int revokeAllForUser(Long userId) {
        return this.sessionRepository.revokeAllByUserId(userId, LocalDateTime.now());
    }

    @Transactional(readOnly = true)
    public List<Session> activeSessions(Long userId) {
        return this.sessionRepository.findAllByUserIdAndRevokedAtIsNull(userId);
    }
}