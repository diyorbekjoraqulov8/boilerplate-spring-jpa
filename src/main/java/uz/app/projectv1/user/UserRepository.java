package uz.app.projectv1.user;

import org.springframework.data.jpa.repository.JpaRepository;
import uz.app.projectv1.user.entity.UserEntity;

import java.util.Optional;

public interface UserRepository extends JpaRepository<UserEntity, Long> {

    Optional<UserEntity> findByEmail(String email);

    boolean existsByEmail(String email);
}