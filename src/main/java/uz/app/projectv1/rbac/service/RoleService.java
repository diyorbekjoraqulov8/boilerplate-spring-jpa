package uz.app.projectv1.rbac.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uz.app.projectv1.common.exception.NotFoundException;
import uz.app.projectv1.rbac.RoleRepository;
import uz.app.projectv1.rbac.dto.RoleResponse;
import uz.app.projectv1.rbac.dto.RoleResponseWithPermission;
import uz.app.projectv1.rbac.dto.request.RoleRequest;
import uz.app.projectv1.rbac.entity.Role;
import uz.app.projectv1.rbac.mapper.RoleMapper;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class RoleService {
    private final RoleRepository roleRepository;
    private final RoleMapper roleMapper;

    public List<?> getAll(boolean withPermission) {
        if (withPermission) {
            return this.roleMapper.toResponseWithPermissionList(this.roleRepository.findAll());
        } else {
            return this.roleMapper.toResponseList(this.roleRepository.findAll());
        }
    }

    public RoleResponse getOne(Long id) {
        return this.roleRepository.findById(id)
                .map(this.roleMapper::toResponse)
                .orElseThrow(() -> new NotFoundException("Role", id));
    }

    public RoleResponseWithPermission create(RoleRequest request) {
        Role newRole = new Role();
        newRole.setName(request.name());
        newRole.setDescription(request.description());

        this.roleRepository.save(newRole);

        return roleMapper.toResponseWithPermission(newRole);
    }
}
