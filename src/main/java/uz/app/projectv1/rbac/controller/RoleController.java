package uz.app.projectv1.rbac.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import uz.app.projectv1.rbac.dto.RoleResponse;
import uz.app.projectv1.rbac.dto.request.RoleRequest;
import uz.app.projectv1.rbac.service.RoleService;

import java.util.List;

@RestController
@RequestMapping("/roles")
@RequiredArgsConstructor
@Validated
public class RoleController {

    private final RoleService roleService;

    @GetMapping
    public List<?> getAll(@RequestParam(defaultValue = "false") boolean withPermission) {
        return this.roleService.getAll(withPermission);
    }

    @GetMapping("/{id}")
    public RoleResponse findOne(@PathVariable Long id) {
        return this.roleService.getOne(id);
    }

    @PostMapping
    public RoleResponse create(@Valid @RequestBody RoleRequest body) {
        return this.roleService.create(body);
    }
}
