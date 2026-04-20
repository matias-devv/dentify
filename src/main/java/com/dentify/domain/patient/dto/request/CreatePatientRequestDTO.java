package com.dentify.domain.patient.dto.request;

import com.dentify.domain.responsibleadult.dto.ResponsibleAdultDTO;
import com.dentify.domain.patient.enums.CoverageType;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;

import java.time.LocalDate;
import java.util.List;

public record CreatePatientRequestDTO(@NotBlank
                                      @Size(min = 7, max = 8)
                                      String dni,

                                      @NotBlank
                                      @Size(max = 100)
                                      String name,

                                      @NotBlank
                                      @Size(max = 100)
                                      String surname,

                                      @NotNull
                                      @Past
                                      LocalDate dateOfBirth,

                                      @Size(max = 100)
                                      String insurance,

                                      @NotNull
                                      CoverageType coverageType,

                                      @Pattern(regexp = "^[+]?[0-9]{7,15}$")
                                      String phoneNumber,

                                      @Email            //A minor patient may or may not have an email address.
                                      @Size(max = 150)
                                      String email,

                                      @Valid
                                      List<ResponsibleAdultDTO> responsibleAdultList) {
}
