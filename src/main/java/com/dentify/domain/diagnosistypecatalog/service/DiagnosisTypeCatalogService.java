package com.dentify.domain.diagnosistypecatalog.service;

import com.dentify.domain.diagnosistypecatalog.enums.DiagnosisSymbol;
import com.dentify.domain.diagnosistypecatalog.model.DiagnosisTypeCatalog;
import com.dentify.domain.diagnosistypecatalog.repository.IDiagnosisTypeCatalogRepository;
import com.dentify.exception.diagnosistypecatalog.DiagnosisTypeNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DiagnosisTypeCatalogService implements IDiagnosisTypeCatalogService{

    private final IDiagnosisTypeCatalogRepository diagnosisTypeCatalogRepository;

    /**
     * System display name for every global symbol, except CUSTOM, which is reserved
     * for clinic-defined entries and intentionally has no system-level name here.
     */
    private static final Map<DiagnosisSymbol, String> GLOBAL_DIAGNOSIS_NAMES = buildGlobalDiagnosisNames();

    @Override
    public Map<Long, DiagnosisTypeCatalog> resolveAccessibleDiagnoses( Set<Long> requestedDiagnosisIds, Long clinicId) {

        Map<Long, DiagnosisTypeCatalog> accessibleDiagnoses = diagnosisTypeCatalogRepository
                                                                .findAccessibleByIds( requestedDiagnosisIds, clinicId )
                                                                .stream()
                                                                .collect( Collectors.toMap( DiagnosisTypeCatalog::getId, diagnosis -> diagnosis ) );

        Set<Long> missingDiagnosisIds = requestedDiagnosisIds.stream()
                                                             .filter(id -> !accessibleDiagnoses.containsKey( id ) )
                                                             .collect( Collectors.toSet() );

        if ( !missingDiagnosisIds.isEmpty() ) throw new DiagnosisTypeNotFoundException("Diagnosis not found, ids: " + missingDiagnosisIds);

        return accessibleDiagnoses;
    }

    @Override
    @Transactional
    public int seedDiagnosisTypeCatalog() {

        Set<DiagnosisSymbol> existingGlobalSymbols = diagnosisTypeCatalogRepository.findGlobalSymbols();

        List<DiagnosisTypeCatalog> toCreate =
                GLOBAL_DIAGNOSIS_NAMES.entrySet().stream()
                                      .filter( entry -> !existingGlobalSymbols.contains( entry.getKey() ) )
                                      .map( entry -> this.buildGlobalDiagnosisTypeCatalog( entry.getKey(), entry.getValue() ) )
                                      .toList();

        if ( toCreate.isEmpty() ) return 0;

        diagnosisTypeCatalogRepository.saveAll( toCreate );

        return toCreate.size();
    }

    private DiagnosisTypeCatalog buildGlobalDiagnosisTypeCatalog(DiagnosisSymbol symbol, String name) {
        return DiagnosisTypeCatalog.builder()
                                   .name(name)
                                   .symbol(symbol)
                                   .isGlobal(true)
                                   .active(true)
                                   .clinic(null)
                                   .build();
    }

    /**
     * Maps every {@link DiagnosisSymbol} to its default display name, excluding CUSTOM.
     * CUSTOM is excluded on purpose: per the entity's Javadoc, custom entries are
     * clinic-owned (isGlobal = false, clinic != null) and are created through the
     * regular clinic-facing "create diagnosis type" flow, never by this seeder.
     */
    private static Map<DiagnosisSymbol, String> buildGlobalDiagnosisNames() {

        Map<DiagnosisSymbol, String> names = new EnumMap<>(DiagnosisSymbol.class);

        names.put(DiagnosisSymbol.ROOT_CANAL_TREATMENT,    "Root canal treatment");
        names.put(DiagnosisSymbol.INCURABLE_TOOTH_DECAY,   "Untreatable decay");
        names.put(DiagnosisSymbol.MISSING_TOOTH,           "Missing tooth");
        names.put(DiagnosisSymbol.SILICATE_FILLING,        "Silicate filling");
        names.put(DiagnosisSymbol.PARADENTOSIS,            "Periodontosis");
        names.put(DiagnosisSymbol.PERNO,                   "Post");
        names.put(DiagnosisSymbol.BRIDGE,                  "Bridge");
        names.put(DiagnosisSymbol.ORTHODONTICS,            "Orthodontics");
        names.put(DiagnosisSymbol.TREATABLE_DECAY,         "Treatable decay");
        names.put(DiagnosisSymbol.EXTRACTION,              "Extraction");
        names.put(DiagnosisSymbol.AMALGAM_FILLING,         "Amalgam filling");
        names.put(DiagnosisSymbol.ACRYLIC_FILLING,         "Acrylic filling");
        names.put(DiagnosisSymbol.CROWN,                   "Crown");
        names.put(DiagnosisSymbol.INLAY_ONLAY,             "Inlay/onlay");
        names.put(DiagnosisSymbol.REMOVABLE_PROSTHESIS,    "Removable prosthesis");
        names.put(DiagnosisSymbol.IMPLANT,                 "Implant");

        return Collections.unmodifiableMap(names);
    }
}
