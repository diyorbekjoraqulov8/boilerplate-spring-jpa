package uz.app.projectv1.rbac.controller;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import uz.app.projectv1.rbac.dto.RoleResponse;
import uz.app.projectv1.rbac.service.RoleService;

import java.util.List;

@RestController
@RequestMapping("/roles")
@RequiredArgsConstructor
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
}
