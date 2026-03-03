package com.dentify.domain.patient.dto.response;

import com.dentify.domain.patient.enums.CoverageType;

import java.time.LocalDate;

public record PatientResponse(Long id,
                              String name,
                              String surname,
                              String dni,
                              String phoneNumber,
                              LocalDate dateOfBirth,
                              CoverageType coverageType) {
}
