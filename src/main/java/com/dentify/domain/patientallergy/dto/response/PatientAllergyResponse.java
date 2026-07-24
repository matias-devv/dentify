package com.dentify.domain.patientallergy.dto.response;

public record PatientAllergyResponse(Long id,
                                     Long allergyId,
                                     String allergyName,
                                     String notes) {
}
