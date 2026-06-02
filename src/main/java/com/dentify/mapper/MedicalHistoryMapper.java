package com.dentify.mapper;

import com.dentify.domain.dentist.model.Dentist;
import com.dentify.domain.medicalhistory.dto.request.CreateMedicalHistoryRequest;
import com.dentify.domain.medicalhistory.dto.response.CreateMedicalHistoryResponse;
import com.dentify.domain.medicalhistory.model.MedicalHistory;
import com.dentify.domain.patient.model.Patient;
import com.dentify.domain.patientallergy.dto.response.PatientAllergyResponse;
import com.dentify.domain.patientallergy.model.PatientAllergy;
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
                            .daily_Medication(request.getDailyMedication())
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
                                                medicalHistory.getDaily_Medication(),
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
}
