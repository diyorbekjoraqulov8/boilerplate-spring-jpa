package uz.app.projectv1.rbac.dto.request;

import java.util.Set;

public record RoleUpdateRequest(
        String description,
        Set<Long> permissionIds
) {}
