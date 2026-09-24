package uz.app.projectv1.auth.dto;

import java.time.LocalDateTime;
import java.util.UUID;

public record SessionResponse(
        UUID id,
        String device,
        String ip,
        LocalDateTime createdDate,
        LocalDateTime lastSeenAt,
        boolean current
) {}