package com.dentify.domain.payment.dto.projections;

public interface CollectionRateProjection {

    Long getTotalPaymentsMonth();

    Long getConfirmedPayments();

    Long getPendingPayments();
}
