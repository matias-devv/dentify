package com.dentify.mapper;

import com.dentify.domain.complementaryexam.dto.response.ComplementaryExamResponse;
import com.dentify.domain.complementaryexam.model.ComplementaryExam;
import com.dentify.domain.dentist.dto.DentistDetailResponse;
import com.dentify.domain.dentist.model.Dentist;
import com.dentify.domain.diagnosistypecatalog.dto.response.DiagnosisTypeCatalogResponse;
import com.dentify.domain.diagnosistypecatalog.model.DiagnosisTypeCatalog;
import com.dentify.domain.medicalhistory.dto.request.CreateMedicalHistoryRequest;
import com.dentify.domain.medicalhistory.dto.response.CreateMedicalHistoryResponse;
import com.dentify.domain.medicalhistory.dto.response.MedicalHistoryDetailResponse;
import com.dentify.domain.medicalhistory.dto.response.MedicalHistorySummaryResponse;
import com.dentify.domain.medicalhistory.model.MedicalHistory;
import com.dentify.domain.patient.dto.response.PatientDetailResponse;
import com.dentify.domain.patient.model.Patient;
import com.dentify.domain.patientallergy.dto.response.PatientAllergyDetailResponse;
import com.dentify.domain.patientallergy.dto.response.PatientAllergyResponse;
import com.dentify.domain.patientallergy.model.PatientAllergy;
import com.dentify.domain.toothrecord.dto.response.ToothRecordResponse;
import com.dentify.domain.toothrecord.model.ToothRecord;
import com.dentify.domain.userProfile.dto.response.SimpleUserProfileResponse;
import com.dentify.domain.userProfile.model.UserProfile;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Component
public class MedicalHistoryMapper {

    public MedicalHistory buildMedicalHistory(Dentist dentist, Patient patient, CreateMedicalHistoryRequest request) {
        return MedicalHistory.builder()
                            .startDate(request.getStartDate())
                            .odontogramType(request.getOdontogramType())
                            .pastMedicalHistory(request.getPastMedicalHistory())
                            .observations(request.getObservations())
                            .hasAllergies(request.getHasAllergies())
                            .dailyMedication(request.getDailyMedication())
                            .dentist(dentist)
                            .patient(patient)
                            .editedBy(null)
                            .build();
    }

    public CreateMedicalHistoryResponse buildCreateMedicalHistoryResponse(MedicalHistory medicalHistory) {

        List<PatientAllergyResponse> allergyResponseList = ( medicalHistory.getAllergies() != null ) ?
                                                                                          this.buildPatientAllergyResponseList( medicalHistory.getAllergies() )
                                                                                          : null;

        return new CreateMedicalHistoryResponse(medicalHistory.getId(),
                                                medicalHistory.getStartDate(),
                                                medicalHistory.getOdontogramType().name(),
                                                medicalHistory.getPastMedicalHistory(),
                                                medicalHistory.getObservations(),
                                                medicalHistory.getHasAllergies(),
                                                medicalHistory.getDailyMedication(),
                                                allergyResponseList,
                                                medicalHistory.getDentist().getId(),
                                                medicalHistory.getDentist().getUserProfile().getName(),
                                                medicalHistory.getDentist().getUserProfile().getSurname(),
                                                medicalHistory.getPatient().getId_patient(),
                                                medicalHistory.getPatient().getName(),
                                                medicalHistory.getPatient().getSurname(),
                                                LocalDateTime.now() );
    }

    private List<PatientAllergyResponse> buildPatientAllergyResponseList(List<PatientAllergy> allergies) {

        List<PatientAllergyResponse> patientAllergyResponseList = new ArrayList<>();

        for ( PatientAllergy allergy : allergies ){
            patientAllergyResponseList.add( this.buildPatientAllergyResponseObject(allergy) );
        }
        return patientAllergyResponseList;
    }

    private PatientAllergyResponse buildPatientAllergyResponseObject(PatientAllergy allergy) {
        return new PatientAllergyResponse( allergy.getId(),
                                           allergy.getAllergy().getId(),
                                           allergy.getAllergy().getName(),
                                           allergy.getNotes() );
    }

    public MedicalHistorySummaryResponse toSummaryResponse(MedicalHistory medicalHistory, int toothRecordCount, int allergyCount, int examCount) {

        return new MedicalHistorySummaryResponse(medicalHistory.getId(),
                                                medicalHistory.getStartDate(),
                                                medicalHistory.getOdontogramType(),
                                                medicalHistory.getObservations(),
                                                medicalHistory.getPastMedicalHistory(),
                                                medicalHistory.getHasAllergies(),
                                                medicalHistory.getDailyMedication(),
                                                buildDentistSummary( medicalHistory.getDentist() ),
                                                buildEditedBySummary( medicalHistory.getEditedBy() ),
                                                allergyCount,
                                                toothRecordCount,
                                                examCount );
    }

    private MedicalHistorySummaryResponse.DentistSummary buildDentistSummary(Dentist dentist) {

        String fullName = dentist.getUserProfile().getName() + " " + dentist.getUserProfile().getSurname();

        return new MedicalHistorySummaryResponse.DentistSummary(dentist.getId(), fullName);
    }

    private MedicalHistorySummaryResponse.UserProfileSummary buildEditedBySummary(UserProfile editedBy) {

        if (editedBy == null) return null;

        String fullName = editedBy.getName() + " " + editedBy.getSurname();

        return new MedicalHistorySummaryResponse.UserProfileSummary(editedBy.getId(), fullName);
    }

    public MedicalHistoryDetailResponse buildMedicalHistoryDetailResponse(MedicalHistory medicalHistory, Patient patient,
                                                                           List<PatientAllergyDetailResponse> allergies) {

        UserProfile editedBy = (medicalHistory.getEditedBy() != null) ? medicalHistory.getEditedBy() : null;

        PatientDetailResponse patientResponse = this.mapPatient(patient);

        DentistDetailResponse dentistResponse = this.mapDentist(medicalHistory.getDentist());

        SimpleUserProfileResponse editedByResponse = this.mapSimpleUserProfile(editedBy);

        List<ToothRecordResponse> toothRecords = ( medicalHistory.getToothRecords() != null ) ? this.mapToothRecords( medicalHistory.getToothRecords() ) : List.of();

        List<ComplementaryExamResponse> complementaryExams = ( medicalHistory.getExams() != null ) ? this.mapComplementaryExams( medicalHistory.getExams() ) : List.of();

        return new MedicalHistoryDetailResponse(medicalHistory.getId(),
                                                medicalHistory.getStartDate(),
                                                medicalHistory.getOdontogramType(),
                                                medicalHistory.getPastMedicalHistory(),
                                                medicalHistory.getObservations(),
                                                medicalHistory.getHasAllergies(),
                                                medicalHistory.getDailyMedication(),
                                                patientResponse,
                                                dentistResponse,
                                                editedByResponse,
                                                toothRecords,
                                                allergies,
                                                complementaryExams);
    }

    private PatientDetailResponse mapPatient(Patient patient) {

        return new PatientDetailResponse(patient.getId_patient(),
                                         patient.getName(),
                                         patient.getSurname(),
                                         patient.getDni(),
                                         patient.getDate_of_birth().toString(),
                                         ( patient.getPhone_number() != null ) ? patient.getPhone_number() : null,
                                         patient.getEmail(),
                                         (patient.getCoverageType() != null) ? patient.getCoverageType().name() : null,
                                         patient.getInsurance());
    }

    private DentistDetailResponse mapDentist(Dentist dentist) {

        return new DentistDetailResponse(dentist.getId(),
                                        dentist.getUserProfile().getName(),
                                        dentist.getUserProfile().getSurname(),
                                        dentist.getProfessional_license());
    }

    private SimpleUserProfileResponse mapSimpleUserProfile(UserProfile editedBy) {

        if (editedBy == null) {
            return null;
        }

        return new SimpleUserProfileResponse(editedBy.getId(),
                                             editedBy.getName(),
                                             editedBy.getSurname() );
    }

    private List<ToothRecordResponse> mapToothRecords( List<ToothRecord> toothRecords ) {
        if (toothRecords == null || toothRecords.isEmpty()) {
            return List.of();
        }
        return toothRecords.stream()
                .map(this::mapToothRecord)
                .toList();
    }

    private ToothRecordResponse mapToothRecord(ToothRecord record) {

        DiagnosisTypeCatalog catalog = record.getDiagnosisType();

        DiagnosisTypeCatalogResponse diagnosisResponse = ( catalog != null ) ? this.buildTypeCatalogResponse(catalog) : null;

        return new ToothRecordResponse(record.getId(),
                                       record.getPieceNumber(),
                                       record.getRecordType().name(),
                                       record.getFace().name(),
                                       record.getObservations(),
                                       record.getCreatedAt().toString(),
                                       diagnosisResponse );
    }

    private DiagnosisTypeCatalogResponse buildTypeCatalogResponse(DiagnosisTypeCatalog catalog) {
        return new DiagnosisTypeCatalogResponse(catalog.getId(),
                                                catalog.getName(),
                                                catalog.getSymbol().name(),
                                                catalog.getIsGlobal(),
                                                catalog.getActive() ) ;
    }

    private List<ComplementaryExamResponse> mapComplementaryExams(List<ComplementaryExam> exams) {

        if (exams == null || exams.isEmpty()) {
            return List.of();
        }
        return exams.stream()
                    .map(this::mapComplementaryExam)
                    .toList();
    }

    private ComplementaryExamResponse mapComplementaryExam(ComplementaryExam exam) {

        UserProfile uploader = exam.getUploadBy();

        SimpleUserProfileResponse uploaderProfileResponse = this.mapSimpleUserProfile( uploader);

        return new ComplementaryExamResponse(exam.getId(),
                                             exam.getFile_url(),
                                             exam.getFilename(),
                                             exam.getFileType(),
                                             exam.getUploadDate(),
                                             uploaderProfileResponse );
    }

}
