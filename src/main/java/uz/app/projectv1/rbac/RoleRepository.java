package uz.app.projectv1.rbac;

import org.springframework.data.jpa.repository.JpaRepository;
import uz.app.projectv1.rbac.entity.Role;

import java.util.Optional;

public interface RoleRepository extends JpaRepository<Role, Long> {

    Optional<Role> findByName(String name);

    boolean existsByName(String name);
}
