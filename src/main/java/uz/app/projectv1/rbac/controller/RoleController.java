package uz.app.projectv1.rbac.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import uz.app.projectv1.rbac.dto.RoleResponse;
import uz.app.projectv1.rbac.dto.RoleResponseWithPermission;
import uz.app.projectv1.rbac.dto.request.RoleRequest;
import uz.app.projectv1.rbac.dto.request.RoleUpdateRequest;
import uz.app.projectv1.rbac.service.RoleService;

import java.util.List;

@RestController
@RequestMapping("/roles")
@RequiredArgsConstructor
@Validated
public class RoleController {

    private final RoleService roleService;

    @GetMapping
    public List<RoleResponse> getAll() {
        return this.roleService.getAll();
    }

    @GetMapping("/detailed")
    public List<RoleResponseWithPermission> getAllWithPermissions() {
        return this.roleService.getAllWithPermissions();
    }

    @GetMapping("/{id}")
    public RoleResponse findOne(@PathVariable Long id) {
        return this.roleService.getOne(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public RoleResponse create(@Valid @RequestBody RoleRequest body) { return this.roleService.create(body); }

    @PutMapping("/{id}")
    public RoleResponse update(@PathVariable Long id, @Valid @RequestBody RoleUpdateRequest body) {
        return this.roleService.update(id, body);
    }
}
