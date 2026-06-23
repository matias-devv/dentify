package com.dentify.domain.medicalhistory.dto.request;

import com.dentify.domain.toothrecord.enums.OdontogramType;
import jakarta.validation.constraints.Size;

public record EditMedicalHistoryRequest(@Size(max = 5000)
                                        String pastMedicalHistory,   // nullable — null explícito borra el valor

                                        @Size(max = 5000)
                                        String observations,         // nullable — null explícito borra el valor

                                        Boolean hasAllergies,        // nullable — si no se envía, no se modifica

                                        @Size(max = 2000)
                                        String dailyMedication,      // nullable — null explícito borra el valor

                                        OdontogramType odontogramType // nullable — si no se envía, no se modifica)
                                        )
{ }
