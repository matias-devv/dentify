package com.dentify.security.service;

import com.dentify.mapper.PermissionMapper;
import com.dentify.security.model.Permission;
import com.dentify.security.repository.IPermissionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
@RequiredArgsConstructor
public class PermissionService implements IPermissionService{

    private final IPermissionRepository permissionRepository;

    private final PermissionMapper permissionMapper;

    @Override
    public List<Permission> findAll() {
        return  permissionRepository.findAll();
    }

    @Override
    public Optional<Permission> findById(Long id) {
        return permissionRepository.findById(id);
    }

    @Override
    public String save(Permission permission) {
        permissionRepository.save(permission);
        return "the permission was updated successfully";
    }

    @Override
    public void deleteById(Long id) {
        permissionRepository.deleteById(id);
    }

    @Override
    public String update(Permission permission) {
        permissionRepository.save(permission);
        return "the permission was updated successfully";
    }

    @Override
    public Map<String, Permission> seedPermissions() {

        List<String> allPermissions = List.of(
                // users
                "USER_INVITE", "USER_DISABLE",
                // patients
                "PATIENT_CREATE", "PATIENT_EDIT", "PATIENT_DELETE",
                // clinic history
                "CLINICAL_HISTORY_READ", "CLINICAL_HISTORY_WRITE",
                // appointments
                "APPOINTMENT_CREATE", "APPOINTMENT_CANCEL", "APPOINTMENT_REASSIGN", "APPOINTMENT_ADMIT",
                "SCHEDULE_VIEW",
                // billing
                "BILLING_VIEW", "BILLING_VIEW_OWN", "BILLING_REGISTER_CASH", "REPORT_VIEW",
                // platform
                "CLINIC_MANAGE", "PLATFORM_METRICS_VIEW");

        Set<Permission> toSave = new HashSet<>();


        Map<String, Permission> permissionMap = this.fillPermissionMap();

        Map<String, Permission> result = new HashMap<>();

        allPermissions.forEach(name -> {

                    Permission permission;

                    if ( !permissionMap.containsKey(name)) {

                        permission = permissionMapper.buildPermission(name);

                        toSave.add(permission);
                    }
                    else {
                        permission = permissionMap.get(name);
                    }
                    result.put( name, permission);
                }
        );

        permissionRepository.saveAll( toSave);

        return result;
    }

    private Map<String, Permission> fillPermissionMap() {

        Map<String, Permission> permissionMap = new HashMap<>();
        List<Permission> permissions = permissionRepository.findAll();

        if ( !permissions.isEmpty() ) permissions.forEach(permission -> { permissionMap.put( permission.getPermissionName(), permission); } );

        return permissionMap;
    }


}
