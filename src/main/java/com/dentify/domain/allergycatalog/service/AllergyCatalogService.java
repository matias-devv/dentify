package com.dentify.domain.allergycatalog.service;

import com.dentify.domain.allergycatalog.dto.response.AllergyCatalogResponse;
import com.dentify.domain.allergycatalog.model.AllergyCatalog;
import com.dentify.domain.allergycatalog.repository.IAllergyCatalogRepository;
import com.dentify.exception.allergycatalog.AllergiesCatalogNotFoundException;
import com.dentify.mapper.AllergyCatalogMapper;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class AllergyCatalogService implements IAllergyCatalogService {

    private final IAllergyCatalogRepository allergyCatalogRepository;

    //mapper
    private final AllergyCatalogMapper allergyCatalogMapper;

    private static final List<String> DENTAL_ALLERGY_CATALOG = List.of(

            // ── Local anesthetics ─────────────────────────────────────────────
            // Most common source of adverse reactions in dentistry
            "Lidocaine",
            "Articaine",
            "Mepivacaine",
            "Prilocaine",
            "Bupivacaine",

            // ── Antibiotics ──────────────────────────────────────────────────
            // First-line and alternative agents used in prophylaxis and infections
            "Penicillin",
            "Amoxicillin",
            "Ampicillin",
            "Clindamycin",
            "Metronidazole",
            "Erythromycin",
            "Cephalexin",

            // ── NSAIDs and analgesics ───────────────────────────────────────
            // Commonly used in postoperative dental care
            "Ibuprofen",
            "Aspirin",
            "Ketorolac",
            "Naproxen",
            "Diclofenac",
            "Acetaminophen (Paracetamol)",
            "Codeine",
            "Tramadol",

            // ── Dental materials ────────────────────────────────────────────
            // Components of prosthetics, resins, and clinical materials
            "Latex",
            "Nickel",
            "Eugenol",
            "Acrylic (PMMA)",
            "Dental Amalgam / Mercury",
            "Composite Resins (BPA)",
            "Chlorhexidine",

            // ── Other relevant substances ───────────────────────────────────
            "Iodine",
            "Sulfites",          // present in vasoconstrictor-containing anesthetics
            "Pollen",
            "Dust Mites"
    );

    @Override
    public List<AllergyCatalog> findAllergiesWithThisIds(List<Long> ids) {
        return allergyCatalogRepository.findAllergiesWithThisIds(ids)
                                       .orElseThrow( () -> new AllergiesCatalogNotFoundException("There is no allergy record for these ids"));
    }

    @Override
    @Transactional
    public int seedAllergies() {

        Set<String> existing = allergyCatalogRepository.findAllNames();

        List<AllergyCatalog> toSave = DENTAL_ALLERGY_CATALOG.stream()
                                                            .filter(name -> !existing.contains(name) )
                                                            .map(name -> AllergyCatalog.builder()
                                                                                             .name(name)
                                                                                             .active(true)
                                                                                             .build() )
                                                            .toList();

        if ( toSave.isEmpty() ) return 0;

        allergyCatalogRepository.saveAll(toSave);

        return toSave.size();
    }

    @Override
    public List<AllergyCatalogResponse> findAllActiveAllergies() {
        return allergyCatalogRepository.findAllActiveOrderByNameAsc()
                                       .stream()
                                       .map(allergyCatalogMapper::toResponse)
                                       .toList();
    }

}
