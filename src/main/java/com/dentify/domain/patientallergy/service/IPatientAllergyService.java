package com.dentify.domain.patientallergy.service;

import com.dentify.domain.medicalhistory.dto.request.CreateMedicalHistoryRequest;
import com.dentify.domain.medicalhistory.model.MedicalHistory;
import com.dentify.domain.patientallergy.model.PatientAllergy;

import java.util.List;

public interface IPatientAllergyService {

    public List<PatientAllergy> processAllergies(CreateMedicalHistoryRequest request, MedicalHistory medicalHistory);
}
