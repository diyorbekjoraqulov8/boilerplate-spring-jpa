package uz.app.projectv1.user.dto;

import java.time.LocalDateTime;

public record UserResponse(
        Long id,
        String email,
        String role,
        boolean active,
        LocalDateTime createdDate
) {}
