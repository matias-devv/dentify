package com.dentify.security.service;

import com.dentify.security.model.Permission;
import com.dentify.security.model.Role;

import java.util.List;
import java.util.Map;
import java.util.Optional;

public interface IRoleService {

    List<Role> getRoles();

    Optional<Role> getRole(Long id);

    String saveRole(Role role);

    void deleteRole(Long id);

    String updateRole(Role role);

    Role getRoleByName(String roleName);

    void seedRole(String roleName, Map<String, Permission> allPermissions, List<String> assignedPermissions);
}
