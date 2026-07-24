package com.dentify.domain.diagnosistypecatalog.seeder;


import com.dentify.domain.diagnosistypecatalog.service.IDiagnosisTypeCatalogService;
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
@Order(4)
public class DiagnosisTypeCatalogSeeder implements ApplicationRunner {

    private final IDiagnosisTypeCatalogService diagnosisTypeCatalogService;

    @Override
    public void run(ApplicationArguments args) {

        int created = diagnosisTypeCatalogService.seedDiagnosisTypeCatalog();

        if (created > 0) {
            log.info("Diagnosis type catalog seeded: {} entries created", created);
        } else {
            log.info("Diagnosis type catalog already seeded, skipping");
        }
    }
}
