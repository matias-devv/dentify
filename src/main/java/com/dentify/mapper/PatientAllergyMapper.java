package com.dentify.mapper;

import com.dentify.domain.allergycatalog.model.AllergyCatalog;
import com.dentify.domain.medicalhistory.model.MedicalHistory;
import com.dentify.domain.patientallergy.dto.response.PatientAllergyDetailResponse;
import com.dentify.domain.patientallergy.dto.response.PatientAllergyResponse;
import com.dentify.domain.patientallergy.model.PatientAllergy;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class PatientAllergyMapper {

    public List<PatientAllergy> buildPatientAllergyList(List<AllergyCatalog> allergiesFromCatalog, MedicalHistory medicalHistory) {

        return allergiesFromCatalog.stream()
                                    .map(allergy -> buildPatientAllergyObject(allergy, medicalHistory) )
                                    .toList();
    }

    private PatientAllergy buildPatientAllergyObject(AllergyCatalog allergyCatalog, MedicalHistory medicalHistory) {

        return PatientAllergy.builder()
                .allergy(allergyCatalog)
                .medicalHistory(medicalHistory)
                .build();
    }

    public PatientAllergyDetailResponse toPatientAllergyDetailResponse(PatientAllergy patientAllergy) {
        return new PatientAllergyDetailResponse( patientAllergy.getId(),
                                                 patientAllergy.getNotes(),
                                                 patientAllergy.getAllergy().getId(),
                                                 patientAllergy.getAllergy().getName(),
                                                 patientAllergy.getAllergy().getActive()) ;
    }

    public List<PatientAllergyResponse> buildPatientAllergyResponseList(List<PatientAllergy> allergies) {

        List<PatientAllergyResponse> patientAllergyResponseList = new ArrayList<>();

        for ( PatientAllergy allergy : allergies ){
            patientAllergyResponseList.add( this.buildPatientAllergyResponseObject(allergy) );
        }
        return patientAllergyResponseList;
    }

    public PatientAllergyResponse buildPatientAllergyResponseObject(PatientAllergy allergy) {
        return new PatientAllergyResponse( allergy.getId(),
                                            allergy.getAllergy().getId(),
                                            allergy.getAllergy().getName(),
                                            allergy.getNotes() );
    }

}
