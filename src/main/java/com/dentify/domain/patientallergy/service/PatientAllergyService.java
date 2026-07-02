package com.dentify.domain.patientallergy.service;

import com.dentify.domain.allergycatalog.model.AllergyCatalog;
import com.dentify.domain.allergycatalog.service.IAllergyCatalogService;
import com.dentify.domain.medicalhistory.dto.request.CreateMedicalHistoryRequest;
import com.dentify.domain.medicalhistory.model.MedicalHistory;
import com.dentify.domain.patientallergy.model.PatientAllergy;
import com.dentify.exception.allergycatalog.AllergiesCatalogNotFoundException;
import com.dentify.mapper.PatientAllergyMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PatientAllergyService implements IPatientAllergyService{

    //services
    private final IAllergyCatalogService allergyCatalogService;
    //mapper
    private final PatientAllergyMapper patientAllergyMapper;

    @Override
    public List<PatientAllergy> processAllergies(CreateMedicalHistoryRequest request, MedicalHistory medicalHistory) {

        List<AllergyCatalog> allergiesFromCatalog = allergyCatalogService.findAllergiesWithThisIds( request.getAllergyIds() );

        this.validateAllRequestedAllergiesWereFound( request.getAllergyIds(), allergiesFromCatalog );

        return patientAllergyMapper.buildPatientAllergyList(allergiesFromCatalog, medicalHistory);
    }

    private void validateAllRequestedAllergiesWereFound(List<Long> requestedIds, List<AllergyCatalog> foundAllergies) {

        Set<Long> foundIds = foundAllergies.stream()
                                            .map(AllergyCatalog::getId)
                                            .collect(Collectors.toSet());

        List<Long> missingIds = requestedIds.stream()
                                            .filter(id -> !foundIds.contains(id))
                                            .toList();

        if ( !missingIds.isEmpty() ) {
            throw new AllergiesCatalogNotFoundException("Some allergy ids were not found or are inactive: " + missingIds );
        }
    }
}
