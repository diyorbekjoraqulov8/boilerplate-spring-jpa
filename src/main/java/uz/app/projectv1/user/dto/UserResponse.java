package uz.app.projectv1.user.dto;

import uz.app.projectv1.rbac.dto.RoleResponse;

import java.time.LocalDateTime;
import java.util.Set;

public record UserResponse(
        Long id,
        String email,
        Set<RoleResponse> roles,
        boolean active,
        LocalDateTime createdDate
) {}
