package uz.app.projectv1.rbac.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uz.app.projectv1.common.exception.ConflictException;
import uz.app.projectv1.common.exception.NotFoundException;
import uz.app.projectv1.rbac.PermissionRepository;
import uz.app.projectv1.rbac.RoleRepository;
import uz.app.projectv1.rbac.dto.RoleResponse;
import uz.app.projectv1.rbac.dto.RoleResponseWithPermission;
import uz.app.projectv1.rbac.dto.request.RoleRequest;
import uz.app.projectv1.rbac.entity.Permission;
import uz.app.projectv1.rbac.entity.Role;
import uz.app.projectv1.rbac.mapper.RoleMapper;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class RoleService {
    private final RoleRepository roleRepository;
    private final RoleMapper roleMapper;
    private final PermissionRepository permissionRepository;

    public List<?> getAll(boolean withPermission) {
        if (withPermission) {
            return this.roleMapper.toResponseWithPermissionList(this.roleRepository.findAllWithPermissions());
        } else {
            return this.roleMapper.toResponseList(this.roleRepository.findAll());
        }
    }

    public RoleResponse getOne(Long id) {
        return this.roleRepository.findById(id)
                .map(this.roleMapper::toResponse)
                .orElseThrow(() -> new NotFoundException("Role", id));
    }

    @Transactional
    public RoleResponse create(RoleRequest request) {
        String roleName = request.name().trim().toUpperCase();

        if (roleRepository.existsByName(roleName)) {
            throw new ConflictException("Rol allaqachon mavjud: " + roleName);
        } else {
            Role newRole = new Role();

            Set<Permission> perms = resolvePermissions(request.permissionIds());

            newRole.setName(roleName);
            newRole.setDescription(request.description());
            newRole.setPermissions(perms);

            this.roleRepository.save(newRole);

            return roleMapper.toResponse(newRole);
        }
    }

    @Transactional
    public RoleResponse update(Long id, RoleRequest request) {
        Role findRole = this.roleRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Role", id));

        if (findRole.isSystemRole()) throw new ConflictException("System rolelarni yangilab bo'lmaydi: " + findRole.getName());
        else {
            Role newRole = new Role();
            newRole.setDescription(request.description());

            Set<Permission> perms = resolvePermissions(request.permissionIds());

            newRole.setPermissions(perms);

            this.roleRepository.save(newRole);

            return roleMapper.toResponse(newRole);
        }
    }

    private Set<Permission> resolvePermissions(Set<Long> ids) {
        if (ids == null || ids.isEmpty()) return new HashSet<>();
        List<Permission> found = this.permissionRepository.findAllById(ids);
        if (found.size() != ids.size()) throw new NotFoundException("Permission", ids);
        return new HashSet<>(found);
    }
}
