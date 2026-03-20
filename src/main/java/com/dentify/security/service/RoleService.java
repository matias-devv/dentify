package com.dentify.security.service;

import com.dentify.mapper.RoleMapper;
import com.dentify.security.model.Permission;
import com.dentify.security.model.Role;
import com.dentify.security.repository.IRoleRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class RoleService implements IRoleService {

    private final IRoleRepository roleRepository;

    private final RoleMapper roleMapper;

    @Override
    public List<Role> getRoles() {
        return roleRepository.findAll();
    }

    @Override
    public Optional<Role> getRole(Long id) {
        return roleRepository.findById(id);
    }

    @Override
    public String saveRole(Role role) {
        roleRepository.save(role);
        return "The role was saved successfully";
    }

    @Override
    public void deleteRole(Long id) {
        roleRepository.deleteById(id);
    }

    @Override
    public String updateRole(Role role) {
        roleRepository.save(role);
        return "The role was updated successfully";
    }

    @Override
    public Role getRoleByName(String roleName) {
        return roleRepository.findByRoleName(roleName).orElseThrow( ()-> new RuntimeException("The role assigned was not found"));
    }

    @Override
    public void seedRole(String roleName, Map<String, Permission> allPermissions, List<String> choosenPermissions) {

        if ( this.verifyIfExists(roleName) ) {
            log.info("Role {} already exists, skipping", roleName);
            return;
        }

        Set<Permission> rolePermissions = new HashSet<>();

        choosenPermissions.forEach(key -> {

                    Permission permission = allPermissions.get(key);

                    if (permission != null) rolePermissions.add(permission);
                }
        );

        Role role = roleMapper.buildRole( roleName, rolePermissions);

        roleRepository.save(role);

        log.info("Role {} created with {} permissions", roleName, rolePermissions.size());

    }

    private boolean verifyIfExists(String roleName) {
        return roleRepository.existsByRoleName( roleName);
    }
}
