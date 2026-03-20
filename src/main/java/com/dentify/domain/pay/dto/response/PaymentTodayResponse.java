package com.dentify.domain.pay.dto.response;

import com.dentify.domain.pay.enums.PaymentMethod;
import com.dentify.domain.pay.enums.PaymentStatus;

import java.math.BigDecimal;

public record PaymentTodayResponse(
        Long          id,
        String        patient_name,
        String        patient_surname,
        Long          patient_id,
        String        hora,             // time of the associated appointment
        BigDecimal monto,
        PaymentMethod medio_pago,       // CASH | MERCADO_PAGO
        PaymentStatus pago_estado,
        Long          appointment_id,
        boolean       hasComprobante) {}
