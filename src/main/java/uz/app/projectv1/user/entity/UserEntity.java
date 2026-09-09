package uz.app.projectv1.user.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import uz.app.projectv1.user.enums.UserRole;

import java.time.LocalDateTime;

@Entity
@Table(name = "users")
@Getter
@Setter
public class UserEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private String password;

    @Enumerated(EnumType.STRING)
    private UserRole role = UserRole.USER;

    @Column(name = "is_active", nullable = false)
    private boolean isActive = true;
    @Column(name = "is_deleted", nullable = false)
    private boolean isDeleted = false;

    @Column(name = "created_date")
    private LocalDateTime createdDate;
    @Column(name = "updated_date")
    private LocalDateTime updatedDate;
}
