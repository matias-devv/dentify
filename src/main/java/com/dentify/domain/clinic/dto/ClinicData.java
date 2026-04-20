package com.dentify.domain.clinic.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record ClinicData(
                         //Clinic details, required for dentist
                          @NotBlank @Size(max = 100) String clinicName,

                          @NotBlank @Size(max = 200) String clinicDirection,

                          @NotBlank @Pattern(regexp = "\\d{2}-\\d{8}-\\d{1}") String clinicCuit,

                          @Size(max = 20) String clinicPhone,    // nullable

                          @Email @Size(max = 100) String clinicEmail )  // nullable)
 {
}
