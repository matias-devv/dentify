package com.dentify.domain.dentist.dto;

public record DentistDetailResponse( Long id,
                                     String name,
                                     String surname,
                                     String professionalLicense) {
}
