package uz.app.projectv1.user;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import uz.app.projectv1.user.entity.UserEntity;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<UserEntity, Long> {

    @Override
    @EntityGraph(attributePaths = "roles")
    List<UserEntity> findAll();

    @Override
    @EntityGraph(attributePaths = "roles")
    Optional<UserEntity> findById(Long id);

    @EntityGraph(attributePaths = "roles")
    Optional<UserEntity> findByEmail(String email);

    @EntityGraph(attributePaths = {"roles", "roles.permissions"})
    Optional<UserEntity> findWithPermissionsByEmail(String email);

    boolean existsByEmail(String email);
}
