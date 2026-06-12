package com.dentify.domain.medicalhistory.service;

import com.dentify.domain.allergycatalog.model.AllergyCatalog;
import com.dentify.domain.allergycatalog.service.IAllergyCatalogService;
import com.dentify.domain.dentist.model.Dentist;
import com.dentify.domain.dentist.service.IDentistService;
import com.dentify.domain.medicalhistory.dto.request.CreateMedicalHistoryRequest;
import com.dentify.domain.medicalhistory.dto.response.CreateMedicalHistoryResponse;
import com.dentify.domain.medicalhistory.dto.response.MedicalHistoryDetailResponse;
import com.dentify.domain.medicalhistory.dto.response.MedicalHistorySummaryResponse;
import com.dentify.domain.medicalhistory.model.MedicalHistory;
import com.dentify.domain.medicalhistory.repository.IMedicalHistoryRepository;
import com.dentify.domain.patient.model.Patient;
import com.dentify.domain.patient.service.IPatientService;
import com.dentify.domain.patientallergy.dto.response.PatientAllergyDetailResponse;
import com.dentify.domain.patientallergy.dto.response.PatientAllergyResponse;
import com.dentify.domain.patientallergy.model.PatientAllergy;
import com.dentify.exception.allergycatalog.AllergiesCatalogNotFoundException;
import com.dentify.exception.medicalhistory.MedicalHistoryNotFoundException;
import com.dentify.exception.patient.PatientNotFoundException;
import com.dentify.mapper.MedicalHistoryMapper;
import com.dentify.mapper.PatientAllergyMapper;
import com.dentify.security.multitenancy.TenantContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.velocity.exception.ResourceNotFoundException;
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

    @Override
    public List<MedicalHistorySummaryResponse> findAllByPatient(Long patientId) {

        this.validatePatientBelongsToTenant(patientId);

        return medicalHistoryRepository.findAllByPatientIdOrderByStartDateDesc(patientId)
                                       .stream()
                                       .map(this::toSummaryWithCounts)
                                       .toList();
    }

    private void validatePatientBelongsToTenant(Long patientId) {

        String patientTenantId = patientService.getTenantIdOrThrow(patientId);

        if ( !patientTenantId.equals( TenantContext.get() ) ) {
            throw new PatientNotFoundException("The patient with this id: " + patientId + " was not found");
        }
    }

    private MedicalHistorySummaryResponse toSummaryWithCounts(MedicalHistory medicalHistory) {

        Long id = medicalHistory.getId();

        int toothRecordCount = medicalHistoryRepository.countToothRecordsByMedicalHistoryId(id);
        int allergyCount     = medicalHistoryRepository.countAllergiesByMedicalHistoryId(id);
        int examCount        = medicalHistoryRepository.countExamsByMedicalHistoryId(id);

        return mapper.toSummaryResponse(medicalHistory, toothRecordCount, allergyCount, examCount);
    }

    @Override
    public MedicalHistoryDetailResponse getMedicalHistoryDetail(Long patientId, Long medicalHistoryId, String username) {

        this.validatePatientBelongsToTenant(patientId);

        Patient patient =  patientService.findPatientById(patientId);

        MedicalHistory medicalHistory = this.findMedicalHistoryBaseById(medicalHistoryId);

        //add necessary collections to the actual object
        medicalHistoryRepository.findWithToothRecords(medicalHistoryId);
        medicalHistoryRepository.findWithAllergies(medicalHistoryId);
        medicalHistoryRepository.findWithExams(medicalHistoryId);

        this.validateOwnershipOfMedicalHistory( medicalHistory.getPatient().getId_patient(), patient.getId_patient() );

        List<PatientAllergyDetailResponse> allergies = this.resolveAllergies(medicalHistory);

        return mapper.buildMedicalHistoryDetailResponse(medicalHistory, patient, allergies);
    }

    public MedicalHistory findMedicalHistoryBaseById(Long medicalHistoryId) {
        return medicalHistoryRepository.findMedicalHistoryBaseById(medicalHistoryId)
                                       .orElseThrow( ()-> new MedicalHistoryNotFoundException("The medical history with this id: " + medicalHistoryId + " was not found") );
    }

    private void validateOwnershipOfMedicalHistory(Long ownerPatientId, Long patientRequestedId) {

        if ( !ownerPatientId.equals(patientRequestedId) ) {
            throw new ResourceNotFoundException("Medical history not found");
        }
    }

    private List<PatientAllergyDetailResponse> resolveAllergies(MedicalHistory medicalHistory) {

        if( medicalHistory.getHasAllergies() == false ) {
            return List.of();
        }

        List<PatientAllergy> allergyEntities = medicalHistory.getAllergies();

        if( allergyEntities == null || allergyEntities.isEmpty() ) {
            return List.of();
        }

        return allergyEntities.stream()
                              .map(patientAllergyMapper::toPatientAllergyDetailResponse)
                              .toList();
    }


}
