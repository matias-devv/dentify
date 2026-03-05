package com.dentify.security.seeder;

import com.dentify.security.service.IAuthUserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Profile("!test")
@Component
@RequiredArgsConstructor
@Slf4j
@Order(2)
public class AdminSeeder implements ApplicationRunner {

    private final IAuthUserService authUserService;

    @Value("${platform.admin.email}")
    private String adminEmail;

    @Value("${platform.admin.password}")
    private String adminPassword;

    @Override
    public void run(ApplicationArguments args) {


        if ( !authUserService.existsByUsername(adminEmail) ){

            authUserService.createPlatformAdmin( adminEmail, adminPassword);

            log.info("Platform admin created");
        }
    }
}