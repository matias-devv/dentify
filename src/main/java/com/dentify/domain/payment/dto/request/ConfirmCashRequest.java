package com.dentify.domain.payment.dto.request;

import java.math.BigDecimal;

public record ConfirmCashRequest(Long id_payment,
                                 BigDecimal amount_received) {
}
