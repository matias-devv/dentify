package com.dentify.domain.medicalhistory.service;

import com.dentify.domain.allergycatalog.model.AllergyCatalog;
import com.dentify.domain.allergycatalog.service.IAllergyCatalogService;
import com.dentify.domain.dentist.model.Dentist;
import com.dentify.domain.dentist.service.IDentistService;
import com.dentify.domain.medicalhistory.dto.request.CreateMedicalHistoryRequest;
import com.dentify.domain.medicalhistory.dto.response.CreateMedicalHistoryResponse;
import com.dentify.domain.medicalhistory.model.MedicalHistory;
import com.dentify.domain.medicalhistory.repository.IMedicalHistoryRepository;
import com.dentify.domain.patient.model.Patient;
import com.dentify.domain.patient.service.IPatientService;
import com.dentify.domain.patientallergy.model.PatientAllergy;
import com.dentify.exception.allergycatalog.AllergiesCatalogNotFoundException;
import com.dentify.mapper.MedicalHistoryMapper;
import com.dentify.mapper.PatientAllergyMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class MedicalHistoryService implements IMedicalHistoryService {

    //repository
    private final IMedicalHistoryRepository medicalHistoryRepository;

    //services
    private final IDentistService dentistService;
    private final IPatientService patientService;
    private final IAllergyCatalogService allergyCatalogService;

    //mappers
    private final MedicalHistoryMapper mapper;
    private final PatientAllergyMapper patientAllergyMapper;

    @Override
    public CreateMedicalHistoryResponse createMedicalHistory(Long patientId, String username, CreateMedicalHistoryRequest request) {

        Dentist dentist = dentistService.findDentistByAuthUserUsername(username);

        Patient patient = patientService.findPatientByIdAndClinicId(patientId, dentist.getClinic().getId() );

        MedicalHistory medicalHistory = mapper.buildMedicalHistory(dentist, patient, request);

        List<PatientAllergy> allergies;

        if ( request.getHasAllergies() && request.getAllergyIds() != null && !request.getAllergyIds().isEmpty() ){

            allergies = this.processAllergies( request.getAllergyIds(), medicalHistory );

            medicalHistory.addAllergies( allergies );
        }
        medicalHistoryRepository.save( medicalHistory );

        return mapper.buildCreateMedicalHistoryResponse(medicalHistory);
    }

    private List<PatientAllergy> processAllergies( List<Long> allergyIds, MedicalHistory medicalHistory) {

        List<AllergyCatalog> allergiesFromCatalog = allergyCatalogService.findAllergiesWithThisIds( allergyIds );

        this.validateAllRequestedAllergiesWereFound( allergyIds, allergiesFromCatalog );

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
