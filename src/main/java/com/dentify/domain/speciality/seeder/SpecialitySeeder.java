package com.dentify.domain.speciality.seeder;

import com.dentify.domain.speciality.service.ISpecialityService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;


@Component
@RequiredArgsConstructor
@Slf4j
@Order(3)
public class SpecialitySeeder implements ApplicationRunner {

    private final ISpecialityService specialityService;

    @Override
    @Transactional
    public void run(ApplicationArguments args) {

        log.info("Seeding specialities...");

        specialityService.seedSpecialities();
    }
}
