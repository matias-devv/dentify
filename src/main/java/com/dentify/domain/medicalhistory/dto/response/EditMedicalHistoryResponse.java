package com.dentify.domain.medicalhistory.dto.response;

import com.dentify.domain.toothrecord.enums.OdontogramType;

import java.time.LocalDate;

public record EditMedicalHistoryResponse(Long id,
                                         LocalDate startDate,
                                         OdontogramType odontogramType,
                                         String pastMedicalHistory,
                                         String observations,
                                         Boolean hasAllergies,
                                         String dailyMedication,
                                         Long dentistId,
                                         String dentistName,
                                         Long patientId,
                                         Long editedById) {
}
