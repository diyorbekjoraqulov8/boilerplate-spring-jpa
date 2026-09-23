package uz.app.projectv1.security;

import java.util.Set;
import java.util.UUID;

public record AuthUser(
        Long id,
        String email,
        UUID sessionId,
        Set<String> roles,
        Set<String> permissions
) {}
