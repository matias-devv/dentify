package com.dentify.domain.patient.dto.response;

public record PatientDetailResponse( Long id,
                                     String name,
                                     String surname,
                                     String dni,
                                     String dateOfBirth,
                                     String phone,
                                     String email,
                                     String coverageType,
                                     String insurance){ }
