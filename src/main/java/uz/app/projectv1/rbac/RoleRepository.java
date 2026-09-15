package uz.app.projectv1.rbac;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import uz.app.projectv1.rbac.entity.Role;

import java.util.List;
import java.util.Optional;

public interface RoleRepository extends JpaRepository<Role, Long> {

    @EntityGraph(attributePaths = "permissions")
    @Query("SELECT r FROM Role r")
    List<Role> findAllWithPermissions();

    Optional<Role> findByName(String name);

    boolean existsByName(String name);
}
