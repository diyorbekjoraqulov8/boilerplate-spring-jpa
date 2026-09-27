package uz.app.projectv1.rbac.service;

import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uz.app.projectv1.audit.AuditEvent;
import uz.app.projectv1.audit.AuditService;
import uz.app.projectv1.audit.entity.AuditLog;
import uz.app.projectv1.common.exception.ConflictException;
import uz.app.projectv1.common.exception.NotFoundException;
import uz.app.projectv1.rbac.PermissionRepository;
import uz.app.projectv1.rbac.Permissions;
import uz.app.projectv1.rbac.RoleRepository;
import uz.app.projectv1.rbac.dto.RoleResponse;
import uz.app.projectv1.rbac.dto.RoleResponseWithPermission;
import uz.app.projectv1.rbac.dto.request.RoleRequest;
import uz.app.projectv1.rbac.dto.request.RoleUpdateRequest;
import uz.app.projectv1.rbac.entity.Permission;
import uz.app.projectv1.rbac.entity.Role;
import uz.app.projectv1.rbac.mapper.RoleMapper;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class RoleService {
    private final RoleRepository roleRepository;
    private final RoleMapper roleMapper;
    private final PermissionRepository permissionRepository;
    private final AuditService auditService;

    @PreAuthorize(Permissions.CAN_READ_ROLE)
    public List<RoleResponse> getAll() {
        return this.roleMapper.toResponseList(this.roleRepository.findAll());
    }

    @PreAuthorize(Permissions.CAN_READ_ROLE)
    public List<RoleResponseWithPermission> getAllWithPermissions() {
        return this.roleMapper.toResponseWithPermissionList(this.roleRepository.findAllWithPermissions());
    }

    @PreAuthorize(Permissions.CAN_READ_ROLE)
    public RoleResponse getOne(Long id) {
        return this.roleRepository.findById(id)
                .map(this.roleMapper::toResponse)
                .orElseThrow(() -> new NotFoundException("Role", id));
    }

    @PreAuthorize(Permissions.CAN_CREATE_ROLE)
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

            auditService.event(AuditEvent.ROLE_CREATED)
                    .target("ROLE", newRole.getId())
                    .detail("roleName", roleName)
                    .record();

            return roleMapper.toResponse(newRole);
        }
    }

    @PreAuthorize(Permissions.CAN_UPDATE_ROLE)
    @Transactional
    public RoleResponse update(Long id, RoleUpdateRequest request) {
        Role role = this.roleRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Role", id));

        if (role.isSystemRole())
            throw new ConflictException("System rolelarni yangilab bo'lmaydi: " + role.getName());

        Set<String> oldPerms = role.getPermissions().stream().map(Permission::getName)
                .collect(Collectors.toSet());

        Set<Long> newPerms = request.permissionIds() == null ? Set.of() : request.permissionIds();

        role.setDescription(request.description());
        role.setPermissions(resolvePermissions(request.permissionIds()));

        auditService.event(AuditEvent.ROLE_UPDATED)
                .target("ROLE", id)
                .detail("roleName", role.getName())
                .detail("permissionsBefore", oldPerms)
                .detail("permissionsAfter", newPerms)
                .record();

        return roleMapper.toResponse(role);
    }

    private Set<Permission> resolvePermissions(Set<Long> ids) {
        if (ids == null || ids.isEmpty()) return new HashSet<>();
        List<Permission> found = this.permissionRepository.findAllById(ids);
        if (found.size() != ids.size()) throw new NotFoundException("Permission", ids);
        return new HashSet<>(found);
    }
}
