package uz.app.projectv1.user;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.mapstruct.ReportingPolicy;
import uz.app.projectv1.rbac.entity.Permission;
import uz.app.projectv1.rbac.entity.Role;
import uz.app.projectv1.user.dto.UserRequest;
import uz.app.projectv1.user.dto.UserResponse;
import uz.app.projectv1.user.entity.UserEntity;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.ERROR)
public interface UserMapper {

//    @Mapping(target = "permissions", source = "roles", qualifiedByName = "toPermissionNames")
//    @Mapping(target = "roles",       source = "roles", qualifiedByName = "toRoleNames")
    UserResponse toResponse(UserEntity entity);

    List<UserResponse> toResponseList(List<UserEntity> entities);

    @Named("toRoleNames")
    default Set<String> rolesToNames(Set<Role> roles) {
        return roles == null ? Set.of()
                : roles.stream().map(Role::getName).collect(Collectors.toSet());
    }

    @Named("toPermissionNames")
    default Set<String> toPermissionNames(Set<Role> roles) {
        return roles == null ? Set.of()
                : roles.stream().flatMap(r -> r.getPermissions().stream())
                .map(Permission::getName).collect(Collectors.toSet());
    }

    @Mapping(target = "id",          ignore = true)
    @Mapping(target = "createdDate", ignore = true)
    @Mapping(target = "updatedDate", ignore = true)
    @Mapping(target = "deleted",     ignore = true)
    @Mapping(target = "password",    ignore = true)
    @Mapping(target = "roles",       ignore = true)
    @Mapping(target = "active",      ignore = true)
    UserEntity toEntity(UserRequest request);
}
