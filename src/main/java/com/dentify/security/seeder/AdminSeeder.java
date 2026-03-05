package com.dentify.domain.userProfile.service;

import com.dentify.security.service.IAuthUserService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Profile("dev | prod")  // solo corre en producción, no en tests
@Component
@RequiredArgsConstructor
public class AdminSeeder implements ApplicationRunner {

    private final IAuthUserService authUserService;

    @Value()

    @Value()

    @Override
    public void run(ApplicationArguments args) {

        // El if es el "una sola vez" — si ya existe, no hace nada
        if ( !authUserService.existsByUsername("admin@dentify.com") ){

            authUserService.createPlatformAdmin(
                    "admin@dentify.com",
                    System.getenv("ADMIN_PASSWORD")  // viene de variable de entorno, nunca hardcodeada
            );

            log.info("Platform admin created");
        }
    }
}