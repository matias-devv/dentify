package com.dentify.mapper;

import com.dentify.security.model.Permission;
import org.springframework.stereotype.Component;

@Component
public class PermissionMapper {

    public Permission buildPermission(String name) {
        return Permission.builder()
                        .permissionName(name)
                        .build();
    }
}
