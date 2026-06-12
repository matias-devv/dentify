package com.dentify.domain.patientallergy.dto.response;

public record PatientAllergyDetailResponse (Long id,
                                            String notes,
                                            Long allergyId,
                                            String allergyName,
                                            Boolean isAllergyActive){
}
