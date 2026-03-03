package com.dentify.domain.treatment.dto;

import com.dentify.domain.treatment.enums.TreatmentStatus;

import java.math.BigDecimal;

public record TreatmentResponse(Long id,
                                TreatmentStatus status,
                                BigDecimal pendingBalance) {
}
