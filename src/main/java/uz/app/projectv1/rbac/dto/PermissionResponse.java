package uz.app.projectv1.rbac.dto;

import java.time.LocalDateTime;

public record PermissionResponse(
        Long id,
        String name,
        String description,
        LocalDateTime createdDate
) {}
