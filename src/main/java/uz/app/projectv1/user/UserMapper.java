package uz.app.projectv1.user;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;
import uz.app.projectv1.user.dto.UserRequest;
import uz.app.projectv1.user.dto.UserResponse;
import uz.app.projectv1.user.entity.UserEntity;

import java.util.List;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.ERROR)
public interface UserMapper {

    UserResponse toResponse(UserEntity entity);

    List<UserResponse> toResponseList(List<UserEntity> entities);

    @Mapping(target = "id",          ignore = true)
    @Mapping(target = "createdDate", ignore = true)
    @Mapping(target = "updatedDate", ignore = true)
    @Mapping(target = "deleted",     ignore = true)
    @Mapping(target = "password",    ignore = true)
    @Mapping(target = "role",        ignore = true)
    @Mapping(target = "active",      ignore = true)
    UserEntity toEntity(UserRequest request);
}
