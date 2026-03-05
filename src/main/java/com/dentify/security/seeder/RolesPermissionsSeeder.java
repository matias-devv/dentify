package com.dentify.security.seeder;

import com.dentify.security.model.Permission;
import com.dentify.security.service.IPermissionService;
import com.dentify.security.service.IRoleService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.util.*;

@Component
@RequiredArgsConstructor
@Slf4j
@Order(1)
public class RolesPermissionsSeeder implements ApplicationRunner {

    private final IRoleService roleService;
    private final IPermissionService permissionService;

    // this seeder must run before AdminSeeder
    @Override
    @Transactional
    public void run(ApplicationArguments args) {

        log.info("Seeding roles and permissions...");

        //create permissions
        Map<String, Permission> permissions = permissionService.seedPermissions();

        //create roles with assigned permissions
        roleService.seedRole("ADMIN", permissions,
                List.of("USER_INVITE", "USER_DISABLE", "BILLING_VIEW", "REPORT_VIEW", "CLINIC_MANAGE", "PLATFORM_METRICS_VIEW"));

        roleService.seedRole("DENTIST", permissions, List.of("USER_INVITE", "USER_DISABLE",
                "PATIENT_CREATE", "PATIENT_EDIT", "PATIENT_DELETE",
                "CLINICAL_HISTORY_READ", "CLINICAL_HISTORY_WRITE",
                "APPOINTMENT_CREATE", "APPOINTMENT_CANCEL", "APPOINTMENT_REASSIGN",
                "APPOINTMENT_ADMIT", "SCHEDULE_VIEW", "BILLING_VIEW",
                "BILLING_REGISTER_CASH", "REPORT_VIEW"));

        roleService.seedRole("SECRETARY", permissions, List.of("PATIENT_CREATE", "PATIENT_EDIT", "PATIENT_DELETE",
                "APPOINTMENT_CREATE", "APPOINTMENT_CANCEL",
                "APPOINTMENT_REASSIGN", "APPOINTMENT_ADMIT",
                "SCHEDULE_VIEW", "BILLING_VIEW_OWN", "BILLING_REGISTER_CASH")
        );

    }

}