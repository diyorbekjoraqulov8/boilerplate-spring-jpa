package uz.app.projectv1.user.dto;

import uz.app.projectv1.user.UserMapper;
import uz.app.projectv1.user.entity.UserEntity;

import java.time.LocalDateTime;

public record UserResponse(
        Long id,
        String email,
        String role,
        boolean active,
        LocalDateTime createdDate
) {}
