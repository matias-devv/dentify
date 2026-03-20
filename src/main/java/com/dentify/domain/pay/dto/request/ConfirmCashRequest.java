package com.dentify.domain.pay.dto.request;

import java.math.BigDecimal;

public record ConfirmCashRequest(Long id_payment,
                                 BigDecimal amount_received) {
}
