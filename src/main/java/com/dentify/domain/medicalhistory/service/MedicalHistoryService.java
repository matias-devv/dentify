package com.dentify.domain.medicalhistory.service;

import com.dentify.domain.allergycatalog.model.AllergyCatalog;
import com.dentify.domain.allergycatalog.service.IAllergyCatalogService;
import com.dentify.domain.clinic.service.IClinicService;
import com.dentify.domain.dentist.model.Dentist;
import com.dentify.domain.dentist.service.IDentistService;
import com.dentify.domain.medicalhistory.dto.request.CreateMedicalHistoryRequest;
import com.dentify.domain.medicalhistory.dto.request.EditMedicalHistoryRequest;
import com.dentify.domain.medicalhistory.dto.response.CreateMedicalHistoryResponse;
import com.dentify.domain.medicalhistory.dto.response.EditMedicalHistoryResponse;
import com.dentify.domain.medicalhistory.dto.response.MedicalHistoryDetailResponse;
import com.dentify.domain.medicalhistory.dto.response.MedicalHistorySummaryResponse;
import com.dentify.domain.medicalhistory.model.MedicalHistory;
import com.dentify.domain.medicalhistory.repository.IMedicalHistoryRepository;
import com.dentify.domain.patient.model.Patient;
import com.dentify.domain.patient.service.IPatientService;
import com.dentify.domain.patientallergy.dto.response.PatientAllergyDetailResponse;
import com.dentify.domain.patientallergy.model.PatientAllergy;
import com.dentify.domain.patientallergy.service.IPatientAllergyService;
import com.dentify.domain.toothrecord.dto.request.AddToothRecordsRequest;
import com.dentify.domain.toothrecord.dto.response.ToothRecordResponse;
import com.dentify.domain.toothrecord.model.ToothRecord;
import com.dentify.domain.toothrecord.service.IToothRecordService;
import com.dentify.exception.medicalhistory.MedicalHistoryNotFoundException;
import com.dentify.exception.medicalhistory.OdontogramTypeConflictException;
import com.dentify.exception.patient.PatientNotFoundException;
import com.dentify.exception.patientallergy.AllergyInconsistencyException;
import com.dentify.exception.toothrecord.MissingOdontogramTypeException;
import com.dentify.exception.toothrecord.ToothRecordNotFoundException;
import com.dentify.mapper.MedicalHistoryMapper;
import com.dentify.mapper.PatientAllergyMapper;
import com.dentify.security.multitenancy.TenantContext;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.velocity.exception.ResourceNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class MedicalHistoryService implements IMedicalHistoryService {

    //repository
    private final IMedicalHistoryRepository medicalHistoryRepository;

    //services
    private final IDentistService dentistService;
    private final IPatientService patientService;
    private final IPatientAllergyService patientAllergyService;
    private final IClinicService clinicService;
    private final IToothRecordService toothRecordService;

    //mappers
    private final MedicalHistoryMapper mapper;
    private final PatientAllergyMapper patientAllergyMapper;


    @Override
    @Transactional
    public CreateMedicalHistoryResponse createMedicalHistory(Long patientId, String username, CreateMedicalHistoryRequest request) {

        Dentist dentist = dentistService.findDentistByAuthUserUsername( username );

        Patient patient = patientService.findPatientByIdAndClinicId( patientId, dentist.getClinic().getId() );

        MedicalHistory medicalHistory = mapper.buildMedicalHistory( dentist, patient, request );

        this.handleAllergies( request, medicalHistory );

        this.handleToothRecords( request, medicalHistory, dentist.getClinic().getId() );

        medicalHistoryRepository.save( medicalHistory );

        return mapper.buildCreateMedicalHistoryResponse( medicalHistory );
    }

    private void handleAllergies(CreateMedicalHistoryRequest request, MedicalHistory medicalHistory) {

        if( request.getHasAllergies() && request.getAllergyIds() != null && !request.getAllergyIds().isEmpty() ){

            List<PatientAllergy> allergies =  patientAllergyService.processAllergies( request, medicalHistory);

            medicalHistory.addAllergies( allergies );
        }
    }

    private void handleToothRecords( CreateMedicalHistoryRequest request, MedicalHistory medicalHistory, Long clinicId ) {

        if ( request.getToothRecords() != null && !request.getToothRecords().isEmpty() ) {

            List<ToothRecord> toothRecords = toothRecordService.processToothRecords( request.getToothRecords(), request.getOdontogramType(), clinicId, medicalHistory);

            medicalHistory.addToothRecords(toothRecords);
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

        MedicalHistory medicalHistory = this.findMedicalHistoryBaseByIdAndClinicId( medicalHistoryId, patient.getClinic().getId() );

        //add necessary collections to the actual object
        medicalHistoryRepository.findWithToothRecords(medicalHistoryId);
        medicalHistoryRepository.findWithAllergies(medicalHistoryId);
        medicalHistoryRepository.findWithExams(medicalHistoryId);

        this.validateOwnershipOfMedicalHistory( medicalHistory.getPatient().getId_patient(), patient.getId_patient() );

        List<PatientAllergyDetailResponse> allergies = this.resolveAllergies(medicalHistory);

        return mapper.buildMedicalHistoryDetailResponse(medicalHistory, patient, allergies);
    }

    public MedicalHistory findMedicalHistoryBaseByIdAndClinicId(Long medicalHistoryId, Long clinicId) {
        return medicalHistoryRepository.findMedicalHistoryBaseByIdAndClinicId( medicalHistoryId, clinicId )
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

    @Override
    @Transactional
    public EditMedicalHistoryResponse updateMedicalHistory(EditMedicalHistoryRequest request, String username, Long patientId, Long medicalHistoryId) {

        Dentist dentist = dentistService.findDentistByAuthUserUsername(username);

        Patient patient = patientService.findPatientByIdWithClinic(patientId);

        clinicService.verifyIfTheyBelongToTheSameClinic( dentist.getClinic().getId(), patient.getClinic().getId() );

        MedicalHistory medicalHistory = this.findMedicalHistoryBaseByIdAndClinicId( medicalHistoryId, dentist.getClinic().getId() );

        this.verifyIfMedicalHistoryBelongsToThisPatient( medicalHistory, patient.getId_patient() );
        this.checkRequestedOdontogramType(request, medicalHistory);
        this.checkRequestedFieldHasAllergies(request, medicalHistory);

        medicalHistory = mapper.setNewAttributes(request, medicalHistory);

        medicalHistory.setEditedBy( dentist.getUserProfile() );

        medicalHistoryRepository.save(medicalHistory);

        return mapper.buildEditMedicalHistoryResponse(medicalHistory, dentist, patient);
    }

    private void verifyIfMedicalHistoryBelongsToThisPatient( MedicalHistory medicalHistory, Long idPatient) {

        if ( !medicalHistory.getPatient().getId_patient().equals(idPatient) ) {
            throw new PatientNotFoundException("The medical history of the patient provided was not found");
        }
    }

    private void checkRequestedOdontogramType(EditMedicalHistoryRequest request, MedicalHistory medicalHistory) {

        if ( request.odontogramType() == null ) return;

        if( !request.odontogramType().equals( medicalHistory.getOdontogramType() ) ) {

            //if not empty
            if( !medicalHistory.isToothRecordsListEmpty() ){
                throw new OdontogramTypeConflictException("Cannot change odontogram type: this clinical history already has tooth records. Delete them first");
            }
        }
        //If no tooth records exist -> changing the odontogram type is freely permitted
    }

    private void checkRequestedFieldHasAllergies(EditMedicalHistoryRequest request, MedicalHistory medicalHistory) {

        if( request.hasAllergies() == null ) return;

        if( request.hasAllergies().equals(false) ){

            //if not empty
            if( !medicalHistory.isAllergiesListEmpty() ) {
                throw new AllergyInconsistencyException("Cannot set hasAllergies to false: this medical history has active allergy records. Remove them first.");
            }
        }
        //If no allergy records exist -> changing to false the field "hasAllergies" is freely permitted
    }

    /**
     * Orchestrates {@code POST /api/medical-histories/{medicalHistoryId}}: resolves the acting dentist, loads the target (already existing)
     * {@link MedicalHistory}, guards against a missing {@code odontogramType}, validates and builds the new {@link ToothRecord} entities
     * via {processToothRecords}, appends them to the medical history, persists, and maps
     * the created records to the response contract.
     *
     * <p>No explicit clinic-ownership check is performed here: (one {@code Clinic} maps to exactly one tenant, and cross-clinic access must
     * surface identically to cross-tenant access), the tenant-scoped lookup performed by { findMedicalHistoryBaseByIdAndClinic(Long) }
     * already covers both cases.
     */
    @Override
    @Transactional
    public List<ToothRecordResponse> addToothRecordsToMedicalHistory(AddToothRecordsRequest request, Long medicalHistoryId, String username) {

        Dentist dentist = dentistService.findDentistByAuthUserUsername(username);

        MedicalHistory medicalHistory = this.findMedicalHistoryWithToothRecordsByIdAndClinicId( medicalHistoryId, dentist.getClinic().getId() );

        this.requireOdontogramTypeConfigured(medicalHistory);

        List<ToothRecord> toothRecords = toothRecordService.processToothRecords( request.toothRecordItems(), medicalHistory.getOdontogramType(),
                                                                                 dentist.getClinic().getId(), medicalHistory);
        medicalHistory.addToothRecords(toothRecords);

        medicalHistoryRepository.save(medicalHistory);

        return toothRecordService.toResponseList(toothRecords);
    }

    private MedicalHistory findMedicalHistoryWithToothRecordsByIdAndClinicId(Long medicalHistoryId, Long clinicId) {

        return medicalHistoryRepository.findMedicalHistoryWithToothRecordsByIdAndClinicId( medicalHistoryId, clinicId )
                                       .orElseThrow( ()-> new MedicalHistoryNotFoundException("The medical history with this id: " + medicalHistoryId + " was not found") );
    }

    private void requireOdontogramTypeConfigured(MedicalHistory medicalHistory) {

        if ( medicalHistory.isOdontogramTypeNull() ) {
            throw new MissingOdontogramTypeException("The medical history with this id: " + medicalHistory.getId() + " has no odontogram type configured");
        }
    }

    @Override
    @Transactional
    public void deleteToothRecord(Long medicalHistoryId, Long toothRecordId, String username) {

        Dentist dentist = dentistService.findDentistByAuthUserUsername(username);

        MedicalHistory medicalHistory = this.findMedicalHistoryWithToothRecordsByIdAndClinicId( medicalHistoryId, dentist.getClinic().getId() );

        ToothRecord toothRecord = medicalHistory.getToothRecordOrThrow( toothRecordId );

        medicalHistory.getToothRecords().remove( toothRecord );

        medicalHistoryRepository.save( medicalHistory );
    }

}
