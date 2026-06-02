package com.dentify.domain.medicalhistory.dto.response;

import com.dentify.domain.toothrecord.enums.OdontogramType;

import java.time.LocalDate;

public record MedicalHistorySummaryResponse(Long id,
                                            LocalDate startDate,
                                            OdontogramType odontogramType,
                                            String observations,
                                            String pastMedicalHistory,
                                            Boolean hasAllergies,
                                            String dailyMedication,
                                            DentistSummary dentist,
                                            UserProfileSummary editedBy,   // nullable
                                            int allergyCount,
                                            int toothRecordCount,
                                            int examCount) {

    public record DentistSummary(Long id, String fullName) {}

    public record UserProfileSummary(Long id, String fullName) {}
}