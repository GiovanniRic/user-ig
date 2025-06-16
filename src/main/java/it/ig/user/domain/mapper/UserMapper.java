package it.ig.user.domain.mapper;

import it.ig.user.domain.model.dto.UserDTO;
import it.ig.user.domain.model.entity.RoleEntity;
import it.ig.user.domain.model.entity.UserEntity;
import it.ig.user.domain.model.enums.Role;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Mapper(componentModel = "spring")
public interface UserMapper {
    @Mapping(target = "roles", expression = "java(this.mapRoles(userDTO.roles(), userEntity))")
    @Mapping(target = "id", ignore = true)
    UserEntity toEntity(UserDTO userDTO);

    @Mapping(target = "roles", expression = "java(this.mapRolesEnum(userEntity.getRoles()))")
    UserDTO toDTO(UserEntity userEntity);

    default Set<RoleEntity> mapRoles(Set<Role> roles, UserEntity userEntity) {

        return roles.stream().map(role -> {
            var roleEntity = new RoleEntity();
            roleEntity.setUser(userEntity);
            roleEntity.setRole(role);
            return roleEntity;
        }).collect(Collectors.toSet());
    }

    default Set<Role> mapRolesEnum(Set<RoleEntity> rolesEntity) {
        return rolesEntity.stream().map(RoleEntity::getRole).collect(Collectors.toSet());


    }


}