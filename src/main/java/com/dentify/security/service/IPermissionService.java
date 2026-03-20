package com.dentify.security.service;

import com.dentify.security.model.Permission;

import java.util.List;
import java.util.Map;
import java.util.Optional;

public interface IPermissionService {

    List<Permission> findAll();

    Optional<Permission> findById(Long id);

    String save(Permission permission);

    void deleteById(Long id);

    String update(Permission permission);

    Map<String, Permission> seedPermissions();
}
