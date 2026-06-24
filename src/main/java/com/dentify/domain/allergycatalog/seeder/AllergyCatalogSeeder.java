package com.dentify.domain.allergycatalog.seeder;

import com.dentify.domain.allergycatalog.service.IAllergyCatalogService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Profile("!test")
@Component
@RequiredArgsConstructor
@Slf4j
@Order(3)
public class AllergyCatalogSeeder implements ApplicationRunner {

    private final IAllergyCatalogService allergyCatalogService;

    @Override
    public void run(ApplicationArguments args) {

        int created = allergyCatalogService.seedAllergies();

        if (created > 0) {
            log.info("Allergy catalog seeded: {} entries created", created);
        } else {
            log.info("Allergy catalog already seeded, skipping");
        }
    }
}
