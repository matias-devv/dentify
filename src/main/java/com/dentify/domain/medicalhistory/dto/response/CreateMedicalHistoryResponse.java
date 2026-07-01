package com.dentify.domain.medicalhistory.dto.response;

import com.dentify.domain.patientallergy.dto.response.PatientAllergyResponse;
import com.dentify.domain.toothrecord.dto.response.ToothRecordResponse;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

public record CreateMedicalHistoryResponse(Long idMedicalHistory,
                                           LocalDate startDate,
                                           String odontogramType,
                                           String pastMedicalHistory,
                                           String observations,
                                           boolean hasAllergies,
                                           String dailyMedication,
                                           List<PatientAllergyResponse> allergies,
                                           Long dentistId,
                                           String dentistName,
                                           String dentistSurname,
                                           Long patientId,
                                           String patientName,
                                           String patientSurname,
                                           LocalDateTime createdAt,
                                           List<ToothRecordResponse> toothRecords ) {

    public CreateMedicalHistoryResponse {
        if (toothRecords == null) {
            toothRecords = Collections.emptyList();
        }
    }
}
