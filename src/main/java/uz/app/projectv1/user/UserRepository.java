package uz.app.projectv1.user;

import org.springframework.data.jpa.repository.JpaRepository;
import uz.app.projectv1.user.entity.UserEntity;

public interface UserRepository extends JpaRepository<UserEntity, Long> {
}
