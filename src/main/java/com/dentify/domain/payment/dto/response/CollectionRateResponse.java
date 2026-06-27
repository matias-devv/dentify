package com.dentify.domain.payment.dto.response;

import java.math.BigDecimal;

public record CollectionRateResponse(long totalPaymentsMonth,
                                     long confirmedPayments,
                                     long pendingPayments,
                                     BigDecimal ratePercentage ) {
}
