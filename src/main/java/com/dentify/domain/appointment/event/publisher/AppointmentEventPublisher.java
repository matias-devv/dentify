package com.dentify.domain.appointment.event.publisher;

import com.dentify.domain.appointment.event.AppointmentCreatedWithPaymentEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

/**
 * ✅ Publisher de eventos de dominio
 *
 * Responsabilidad: Publicar eventos de forma sincrónica (Spring maneja el dispatch)
 * Los listeners se ejecutarán de forma asincrónica si están marcados con @Async
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class AppointmentEventPublisher {

    private final ApplicationEventPublisher applicationEventPublisher;

    /**
     * Publica evento de appointment creado con pago
     *
     * Se llama desde AppointmentService.saveAppointmentWithPay() después de
     * crear el appointment y payment en BD.
     *
     * El listener (@EventListener @Async) se ejecuta en thread separado.
     * El método retorna inmediatamente sin esperar al listener.
     *
     * @param event Evento con todos los datos necesarios
     */
    public void publishAppointmentCreatedWithPay(AppointmentCreatedWithPaymentEvent event) {
        log.info("📢 Publishing AppointmentCreatedWithPayEvent: appointmentId={}, paymentMethod={}, shouldConfirmAppointment={}",
                event.getAppointmentId(),
                event.getPaymentMethod(),
                event.getShouldConfirmAppointment());

        applicationEventPublisher.publishEvent(event);

        log.debug("✅ Event published successfully");
    }
}
