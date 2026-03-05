package com.dentify.mapper;

import com.dentify.security.model.Permission;
import com.dentify.security.model.Role;
import org.springframework.stereotype.Component;

import java.util.Set;

@Component
public class RoleMapper {

    public Role buildRole(String roleName, Set<Permission> rolePermissions) {
        return Role.builder()
                .roleName(roleName)
                .permissions(rolePermissions)
                .build();
    }
}
