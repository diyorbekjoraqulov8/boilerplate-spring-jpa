package uz.app.projectv1.rbac.dto;

import java.time.LocalDateTime;
import java.util.Set;

public record RoleResponseWithPermission(
        Long id,
        String name,
        String description,
        boolean systemRole,
        Set<PermissionResponse> permissions,
        LocalDateTime createdDate
) {}
