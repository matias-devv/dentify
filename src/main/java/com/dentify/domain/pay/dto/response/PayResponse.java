package com.dentify.domain.pay.dto.response;

import com.dentify.domain.pay.enums.PaymentMethod;
import com.dentify.domain.pay.enums.PaymentStatus;

import java.math.BigDecimal;

public record PayResponse(Long id,
                          BigDecimal amount,
                          PaymentMethod paymentMethod,
                          PaymentStatus status) {
}
