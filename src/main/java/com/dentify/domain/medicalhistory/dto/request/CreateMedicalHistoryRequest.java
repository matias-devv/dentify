package com.dentify.domain.medicalhistory.dto.request;

import com.dentify.domain.toothrecord.enums.OdontogramType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.util.List;

@Getter @Setter
@AllArgsConstructor @NoArgsConstructor
public class CreateMedicalHistoryRequest {

    @NotNull(message = "the odontogram type is required to proceed")
    private OdontogramType odontogramType;

    @PastOrPresent(message = "the start date cannot be in the future")
    @NotNull
    private LocalDate startDate;

    @Size(max = 5000, message = "past medical history must be at most 5000 characters")
    private String pastMedicalHistory;     // nullable

    @Size(max = 2000, message = "observations must be at most 2000 characters")
    private String observations;           // nullable

    @NotNull(message = "The medical record to be kept needs to know if the patient has allergies or not.")
    private Boolean hasAllergies = false;

    private List<Long> allergyIds = List.of(); // IDs of AllergyCatalog; ignored if hasAllergies = false

    @Size(max = 1000, message = "daily medication must be at most 1000 characters")
    private String dailyMedication;        // nullable

}