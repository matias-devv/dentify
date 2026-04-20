package com.dentify.mapper;

import com.dentify.domain.agenda.model.Agenda;
import com.dentify.domain.appointment.dto.request.CreateAppointmentRequestDTO;
import com.dentify.domain.appointment.dto.response.*;
import com.dentify.domain.appointment.enums.AppointmentStatus;
import com.dentify.domain.appointment.model.Appointment;
import com.dentify.domain.dentist.model.Dentist;
import com.dentify.domain.patient.dto.response.CancelledPatientResponse;
import com.dentify.domain.patient.model.Patient;
import com.dentify.domain.payment.model.TreatmentPayment;
import com.dentify.domain.product.model.Product;
import com.dentify.domain.treatment.model.Treatment;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Component
@RequiredArgsConstructor
public class AppointmentMapper {

    private final ProductMapper productMapper;
    private final PatientMapper patientMapper;
    private final DentistMapper dentistMapper;
    private final AgendaMapper agendaMapper;
    private final TreatmentMapper treatmentMapper;
    private final PaymentMapper paymentMapper;


    public FullAppointmentResponse toResponse(Appointment appointment) {

        if (appointment == null) {
            throw new IllegalArgumentException("Appointment cannot be null");
        }

        return new FullAppointmentResponse( appointment.getId_appointment(),
                                            appointment.getAppointmentStatus(),
                                            appointment.getAppointmentStart(),
                                            appointment.getAppointmentEnd(),
                                            appointment.getDuration_minutes(),
                                            appointment.getAttendanceConfirmed(),
                                            appointment.getConfirmed_at(),

                                            patientMapper.buildPatientResponse(appointment),
                                            productMapper.buildProductResponse(appointment),
                                            dentistMapper.buildDentistResponse(appointment),
                                            agendaMapper.buildSimpleAgendaResponse(appointment),
                                            treatmentMapper.buildTreatmentResponse(appointment),
                                            paymentMapper.buildPayResponse(appointment),

                                            appointment.getNotes(),
                                            appointment.getPatient_instructions(),
                                            appointment.getReason_for_cancellation()
        );
    }


    public AppointmentCancelledResponse toCancelledResponse(Appointment appointment) {

        var patient = appointment.getPatient();

        CancelledPatientResponse patientResponse = new CancelledPatientResponse(patient.getName(),
                                                                                patient.getSurname(),
                                                                                patient.getDni() );

        return new AppointmentCancelledResponse(appointment.getId_appointment(),
                                                appointment.getAppointmentStatus(),
                                                appointment.getAppointmentStart().toLocalDate(),
                                                appointment.getAppointmentStart().toLocalTime(),
                                                appointment.getAppointmentEnd().toLocalTime(),
                                                appointment.getReason_for_cancellation(),
                                                LocalDateTime.now(),
                                                patientResponse );
    }

    public NextAppointment buildNextAppointment(Appointment appointment) {

        var patient = appointment.getPatient();

        LocalDateTime date = appointment.getAppointmentStart();

        return new NextAppointment(date,
                                   patient.getName(),
                                   patient.getSurname() );
    }

    public Appointment buildAppointment(Patient patient, Treatment treatment,
                                        CreateAppointmentRequestDTO request, Dentist dentist, Agenda agenda) {
        return Appointment.builder()
                .notes(request.notes())
                .patient_instructions(request.patient_instructions())
                .appointmentStatus(AppointmentStatus.SCHEDULED)
                .appointmentStart( request.date().atTime( request.start_time() ) )
                .appointmentEnd( request.date().atTime( request.start_time().plusMinutes(request.duration_minutes() ) ) )
                .duration_minutes(request.duration_minutes())
                .attendanceConfirmed(false)
                .clinic(dentist.getClinic())
                .dentist(dentist)
                .patient(patient)
                .treatment(treatment)
                .agenda(agenda)
                .build();
    }

    public CreateAppointmentResponseDTO buildCreateAppointmentResponse(  Product product, TreatmentPayment payment, CreateAppointmentRequestDTO request,
                                                                        Appointment appointment, String paymentLink) {
        return CreateAppointmentResponseDTO.builder()
                .id_appointment(appointment.getId_appointment())
                .id_pay(payment.getId_pay())
                .date( appointment.getAppointmentStart().toLocalDate() )
                .start_time( appointment.getAppointmentStart().toLocalTime() )
                .duration_minutes(request.duration_minutes())
                .end_time( appointment.getAppointmentEnd().toLocalTime() )
                .amount_to_pay(payment.getAmount())
                .payment_method(payment.getPayment_method())
                .payment_link(paymentLink)
                .appointment_status(appointment.getAppointmentStatus())
                .payment_status(payment.getPayment_status())
                .product_name(product.getNameProduct())
                .build();
    }

    public AppointmentResponse buildAppointmentResponse(Appointment appointment) {
        return new AppointmentResponse( appointment.getId_appointment(),
                appointment.getPatient().getName(),
                appointment.getPatient().getSurname(),
                appointment.getAppointmentStatus(),
                appointment.getTreatment().getProduct().getNameProduct() );
    }

    public DetailedAppointmentResponse buildDetailedAppointmentResponse(Appointment appointment) {
        return new DetailedAppointmentResponse(appointment.getId_appointment(),
                appointment.getPatient().getName(),
                appointment.getPatient().getSurname(),
                appointment.getPatient().getPhone_number(),
                appointment.getAppointmentStatus(),
                appointment.getTreatment().getProduct().getNameProduct(),
                appointment.getNotes() );
    }

    public AppointmentTodayResponse buildAppointmentTodayResponse(Appointment a) {

        String serviceName = null;

        if ( a.getTreatment() != null && a.getTreatment().getProduct() != null ) {
            serviceName = a.getTreatment().getProduct().getNameProduct();
        }

        return new AppointmentTodayResponse( a.getId_appointment(),
                                             a.getAppointmentStart().toLocalTime().format(DateTimeFormatter.ofPattern("HH:mm")),
                                             a.getPatient().getName(),
                                             a.getPatient().getSurname(),
                                             a.getPatient().getId_patient(),
                                             a.getPatient().getCoverageType().name(),
                                             a.getAppointmentStatus(),
                                             ( a.getAttendanceConfirmed() != null) ? a.getAttendanceConfirmed() : false,
                                             serviceName);
    }
}