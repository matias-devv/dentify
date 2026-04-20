package com.dentify.integration.email.event;

import com.dentify.domain.appointment.model.Appointment;
import com.dentify.domain.payment.model.TreatmentPayment;

public record CashPaymentCompletedEvent(Appointment appointment, TreatmentPayment payment) {}