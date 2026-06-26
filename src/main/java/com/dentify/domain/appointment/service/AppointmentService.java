package com.dentify.domain.appointment.service;

import com.dentify.domain.appointment.dto.response.AppointmentTodayResponse;
import com.dentify.domain.appointment.dto.response.FullAppointmentResponse;
import com.dentify.domain.appointment.event.AppointmentCreatedWithPaymentEvent;
import com.dentify.domain.appointment.event.publisher.AppointmentEventPublisher;
import com.dentify.domain.dentist.model.Dentist;
import com.dentify.domain.dentist.service.IDentistService;
import com.dentify.domain.mercadopagopayment.service.IMercadoPagoPaymentService;
import com.dentify.domain.patientstat.service.IPatientStatService;
import com.dentify.domain.payment.model.TreatmentPayment;
import com.dentify.domain.userProfile.model.UserProfile;
import com.dentify.domain.userProfile.service.IUserProfileService;
import com.dentify.exception.appointment.AppointmentConflictException;
import com.dentify.exception.appointment.AppointmentNotFoundException;
import com.dentify.exception.appointment.AppointmentStateException;
import com.dentify.exception.pay.PaymentRequiredException;
import com.dentify.integration.email.service.IEmailService;
import com.dentify.mapper.AppointmentMapper;
import com.dentify.domain.agenda.model.Agenda;
import com.dentify.domain.agenda.service.IAgendaService;
import com.dentify.domain.appointment.dto.request.CancelAppointmentRequest;
import com.dentify.domain.appointment.dto.request.CreateAppointmentRequestDTO;
import com.dentify.domain.appointment.dto.response.AppointmentCancelledResponse;
import com.dentify.domain.appointment.model.Appointment;
import com.dentify.domain.appointment.repository.IAppointmentRepository;
import com.dentify.domain.appointment.dto.response.CreateAppointmentResponseDTO;
import com.dentify.domain.appointment.enums.AppointmentStatus;
import com.dentify.domain.patient.service.IPatientService;
import com.dentify.domain.payment.enums.PaymentMethod;
import com.dentify.domain.patient.model.Patient;
import com.dentify.domain.payment.service.ITreatmentPaymentService;
import com.dentify.domain.product.model.Product;
import com.dentify.domain.product.service.IProductService;
import com.dentify.domain.treatment.model.Treatment;
import com.dentify.domain.treatment.service.ITreatmentService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import org.springframework.data.domain.PageRequest;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Service
@Slf4j
@RequiredArgsConstructor
public class AppointmentService implements IAppointmentService {

    //repository
    private final IAppointmentRepository appointmentRepository;

    //services
    private final IEmailService emailService;
    private final ITreatmentService treatmentService;
    private final ITreatmentPaymentService paymentService;
    private final IPatientService patientService;
    private final IProductService productService;
    private final IDentistService dentistService;
    private final IAgendaService agendaService;
    private final IUserProfileService userProfileService;
    private final IPatientStatService patientStatService;
    private final AppointmentEventPublisher eventPublisher;
    //mapper
    private final AppointmentMapper appointmentMapper;

    /**
     * querys
     */
    @Override
    public FullAppointmentResponse getAppointmentById(Long id) {
        Appointment appointment = appointmentRepository.findByIdWithAllDetails(id).orElseThrow(() -> new AppointmentNotFoundException("Appointment not found"));
        return appointmentMapper.toResponse(appointment);
    }

    @Override
    public Appointment findByIdWithReceiptData(Long appointmentId) {
        return appointmentRepository.findByIdWithReceiptData(appointmentId)
                                    .orElseThrow(() -> new AppointmentNotFoundException("Appointment not found"));
    }

    @Override
    public List<Appointment> findScheduledAppointmentsWithDetails(LocalDateTime start, LocalDateTime end) {
        return appointmentRepository.findScheduledAppointmentWithDetails( List.of(AppointmentStatus.SCHEDULED, AppointmentStatus.CONFIRMED),
                start,
                end );
    }

    @Override
    public List<Appointment> findReservedAppointmentsNotConfirmedWithDetails(LocalDateTime start, LocalDateTime end) {
        return appointmentRepository.findReservedAppointmentsNotConfirmedWithDetails( AppointmentStatus.SCHEDULED,
                                                                                    false,
                                                                                      start,
                                                                                      end);
    }

    @Override
    public List<Appointment> findByDateLessThanEqualAndAppointmentStatusInWithDetails(LocalDateTime today, List<AppointmentStatus> statuses) {
        return appointmentRepository.findByDateLessThanEqualAndAppointmentStatusInWithDetails(today, statuses);
    }

    @Override
    public List<Appointment> findAppointmentsByAgendaAndDateRange(Long idAgenda, LocalDateTime start, LocalDateTime end) {
        return appointmentRepository.findAppointmentsByAgendaAndDateRange( idAgenda, start, end );
    }

    @Override
    public List<Appointment> findAppointmentsByAgendaAndDate(Long agendaId, LocalDate date) {
        return appointmentRepository.findAppointmentsByAgendaAndDate(agendaId, date);
    }

    @Override
    public Optional<Appointment> findNextAppointment() {

        List<AppointmentStatus> validStatuses = List.of(AppointmentStatus.SCHEDULED, AppointmentStatus.ADMITTED);

        List<Appointment> result = appointmentRepository.findNextAppointment(LocalDateTime.now(),
                                                                             validStatuses,
                                                                             PageRequest.of(0, 1));

        return result.stream().findFirst();
    }

    @Override
    public Long countAppointmentsTodayExcludingStatuses(List<AppointmentStatus> statuses) {
        return appointmentRepository.countAppointmentsTodayExcludingStatuses( LocalDate.now(), statuses);
    }

    @Override
    public Map<LocalDateTime, Appointment> fillInAppointmentMap(List<Appointment> listAppointments) {

        Map<LocalDateTime, Appointment> map = new HashMap<>();
        if (listAppointments != null) {

            listAppointments.forEach(appo -> {

                LocalDateTime fullTime = appo.getAppointmentStart();

                map.put(fullTime, appo);
            });
        }
        return map;
    }

    @Override
    public Appointment findByIdWithAllEmailData(Long appointmentId) {
        return appointmentRepository.findByIdWithAllEmailData(appointmentId)
                                    .orElseThrow( ()-> new AppointmentNotFoundException("The appointment with this id was not found"));
    }


    @Override
    public List<AppointmentTodayResponse> getAppointmentsByDayForDentist(String username, String requestDate) {

        Dentist dentist = dentistService.findDentistByAuthUserUsername(username);

        List<AppointmentTodayResponse> responses = new ArrayList<>();

        LocalDateTime day = this.resolveActualDate(requestDate);

        LocalDateTime endOfDay = day.with(LocalTime.MAX);

        List<Appointment> appointments = appointmentRepository.findDentistAppointmentsForDayWithDetails(dentist.getId(),
                                                                                                       day,
                                                                                                       endOfDay ,
                                                                                                       List.of(AppointmentStatus.SCHEDULED,
                                                                                                               AppointmentStatus.CONFIRMED,
                                                                                                               AppointmentStatus.ADMITTED,
                                                                                                               AppointmentStatus.IN_ATTENTION,
                                                                                                               AppointmentStatus.COMPLETED,
                                                                                                               AppointmentStatus.NO_SHOW) );

        appointments.forEach(a -> { responses.add( appointmentMapper.buildAppointmentTodayResponse(a) ); } );

        return responses;
    }

    private LocalDateTime resolveActualDate(String date) {

        if ( date != null) {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

            return LocalDate.parse(date, formatter).atStartOfDay();
        }
        return LocalDate.now().atStartOfDay();
    }

    @Override
    public List<Appointment> getCancelledAppointmentsToday(Long dentistId, LocalDateTime startOfDay, LocalDateTime endOfDay) {

        List<AppointmentStatus> cancelledStatuses = AppointmentStatus.getCancelledStatuses();

        return appointmentRepository.findCancelledAppointmentsToday( dentistId, startOfDay, endOfDay, cancelledStatuses);
    }

    // ── Create ────────────────────────────────────────────────────────────────
    @Override
    @Transactional
    public CreateAppointmentResponseDTO saveAppointmentWithPay(CreateAppointmentRequestDTO request) {

        long startTime = System.currentTimeMillis();
        log.info("⏱️  Starting appointment creation for patient: {}", request.id_patient());

        Dentist dentist = dentistService.findByIdWithProfileAndClinic(request.id_dentist());
        Product product = productService.findProductById(request.id_product(), dentist.getClinic().getId() );
        Agenda agenda = agendaService.findAgendaWithSchedules(request.id_agenda());
        Patient patient = patientService.findPatientById(request.id_patient());

        //validations
        this.validateCashPaymentMethodFromRequest(request);

        agendaService.validatAgendaToCreateAppointment(agenda, dentist, request.date(), request.start_time());

        productService.validateIfProductIsActive(product.getActive());

        this.validateAppointmentAvailability(request.date(), request.start_time(), request.duration_minutes(), dentist.getId());

        //create treatment and appointment
        Treatment treatment = treatmentService.findOrCreateTreatment(patient, product, dentist);

        Appointment appointment = appointmentMapper.buildAppointment(patient, request, treatment, dentist, agenda);

        appointmentRepository.save(appointment);

        boolean payNow = (request.payNow() != null) ? request.payNow() : false;

        TreatmentPayment payment = paymentService.handlePaymentCreation(appointment, treatment, request.paymentMethod(), product.getUnitPrice(), payNow);

        //publish async event
        publishAppointmentEvent(appointment, treatment, patient, dentist, product, payNow, payment);

        long duration = System.currentTimeMillis() - startTime;
        log.info("✅ Appointment created in {}ms (events published async)", duration);

        return appointmentMapper.buildCreateAppointmentResponse( product, payment, request, appointment);
    }

    private void validateCashPaymentMethodFromRequest(CreateAppointmentRequestDTO request) {
        if (request.paymentMethod() == PaymentMethod.CASH) {
            if ( request.payNow() == null ) {
                throw new PaymentRequiredException("For cash payments, 'payNow' must be specified");
            }
        }
    }

    /**
     * Construye y publica el evento asincronó
     *
     * El evento contiene TODOS los datos necesarios para el background job
     */
    private void publishAppointmentEvent(Appointment appointment, Treatment treatment, Patient patient, Dentist dentist, Product product,
                                         boolean payNow, TreatmentPayment payment) {
        try {
            //  logica para el paso 6 en el listener( no generar pdfs ni enviar emails
            AppointmentCreatedWithPaymentEvent event = AppointmentCreatedWithPaymentEvent.builder()
                    // Appointment data
                    .appointmentId(appointment.getId_appointment())
                    .appointmentDate(appointment.getAppointmentStart().toLocalDate())
                    .appointmentStartTime(appointment.getAppointmentStart().toLocalTime())
                    .appointmentEndTime(appointment.getAppointmentEnd().toLocalTime())
                    .appointmentNotes(appointment.getNotes())
                    .appointmentStatus(appointment.getAppointmentStatus())
                    .durationMinutes(appointment.getDuration_minutes())

                    // Payment data
                    .paymentId(payment.getId_pay())
                    .paymentMethod( payment.getPayment_method() )
                    .paymentAmount( payment.getAmount())
                    .isPaymentImmediate(payNow)

                    // Patient data
                    .patientEmail(patient.getEmail())
                    .patientName(patient.getName())
                    .patientSurname(patient.getSurname())
                    .patientDni(patient.getDni())
                    .patientInstructions(appointment.getPatient_instructions())

                    // Dentist data
                    .dentistName(dentist.getUserProfile().getName())
                    .dentistSurname(dentist.getUserProfile().getSurname())
                    .dentistEmail(dentist.getUserProfile().getAuthUser().getUsername())
                    .dentistPhone( (dentist.getUserProfile().getPhone_number() != null) ? dentist.getUserProfile().getPhone_number() : null)
                    .clinicName(dentist.getClinic().getName())
                    .tenantId( dentist.getClinic().getTenantId())

                    // Product data
                    .productName(product.getNameProduct())
                    .productDescription(product.getDescription())

                    //treatment
                    .treatmentId( treatment.getId_treatment())

                    // Control flag
                    .shouldConfirmAppointment( payment.isInCash()  && payNow)   // Confirmar appointment si pago inmediato

                    .createdAtMillis(System.currentTimeMillis())
                    .build();

            log.info("📢 Publishing async event for appointment: {}", appointment.getId_appointment());
            eventPublisher.publishAppointmentCreatedWithPay(event);

        } catch (Exception e) {
            log.error("❌ Error publishing appointment event", e);
            // NO relanzar: el appointment ya existe, solo fallaron las notificaciones
        }
    }


    @Override
    public void actualizeAppointmentStatusToConfirmed(Appointment appointment) {

        appointment.setAppointmentStatus( AppointmentStatus.CONFIRMED);

        appointmentRepository.save(appointment);
    }


    // ── State transitions ─────────────────────────────────────────────────────

    /**
     * Admits a patient to their appointment.
     * The appointment can be admitted if:
     * (a) The treatment was fully paid upfront (outstanding_balance == 0), OR
     * (b) There is a PAID Pay linked specifically to this appointment.
     */
    @Override
    @Transactional
    public AppointmentTodayResponse admitPatient(Long appointmentId, String username) {

        UserProfile userProfile = userProfileService.findByAuthUserUsernameWithClinic( username );

        Appointment appointment = this.findByIdWithPatientAndTreatment( appointmentId );

        if ( !appointment.getClinic().getId().equals( userProfile.getClinic().getId() ) )
            throw new AppointmentNotFoundException("The appointment with this id was not found");

        if ( appointment.isAdmited() ) throw new AppointmentStateException("The appointment cannot be admitted if it has already been admitted previously.");

        if ( appointment.isScheduled() || appointment.isConfirmed() || appointment.isWalkInPending() ) {

            if (appointment.getAttendanceConfirmed().equals(false) ) {

                appointment.setAttendanceConfirmed(true);
                appointment.setConfirmed_at(LocalDateTime.now());
            }

            appointment.setAppointmentStatus(AppointmentStatus.ADMITTED);

            patientStatService.recordAdmission( appointment.getPatient().getPatient_stat() );

            appointmentRepository.save(appointment);

            return appointmentMapper.buildAppointmentTodayResponse(appointment);
        }
        throw new AppointmentStateException("The appointment needs to be scheduled or confirmed to be admitted");
    }

    private Appointment findByIdWithPatientAndTreatment(Long appointmentId) {
        return appointmentRepository.findByIdWithPatientAndTreatment(appointmentId)
                                    .orElseThrow(() -> new AppointmentNotFoundException("The appointment with this id was not found"));
    }

    @Override
    public AppointmentTodayResponse startAttention(Long appointmentId, String username) {

        UserProfile userProfile = userProfileService.findByAuthUserUsernameWithClinic( username );

        Appointment appt = this.findByIdWithPatientAndTreatment( appointmentId );

        if ( !appt.getClinic().getId().equals( userProfile.getClinic().getId() ) )
            throw new AppointmentNotFoundException("The appointment with this id was not found");

        if ( !appt.isAdmited()) throw new AppointmentStateException("The appointment needs to be in state ADMITTED to proceed");

        if ( appt.isInAtention()) throw new AppointmentStateException("You cannot mark 'in attention' if it has already been marked with this status previously");

        appt.setAppointmentStatus(AppointmentStatus.IN_ATTENTION);

        appointmentRepository.save(appt);

        return appointmentMapper.buildAppointmentTodayResponse(appt);
    }

    @Override
    public AppointmentTodayResponse completeAppointment(Long appointmentId, String username) {

        UserProfile userProfile = userProfileService.findByAuthUserUsernameWithClinic( username );

        Appointment appt = this.findByIdWithPatientAndTreatment( appointmentId );

        if ( !appt.getClinic().getId().equals( userProfile.getClinic().getId() ) )
            throw new AppointmentNotFoundException("The appointment with this id was not found");

        if ( !appt.isInAtention()) throw new AppointmentStateException("The appointment needs to be in state IN ATTENTION to proceed");

        if ( appt.isCompleted()) throw new AppointmentStateException("You cannot mark 'in attention' if it has already been marked with this status previously");

        appt.setAppointmentStatus(AppointmentStatus.COMPLETED);

        appointmentRepository.save(appt);

        return appointmentMapper.buildAppointmentTodayResponse(appt);
    }

    @Override
    public void markNoShow(Appointment appointment) {

        appointment.setAppointmentStatus(AppointmentStatus.NO_SHOW);

        appointmentRepository.save(appointment);
    }

    /**
     * Internal cancel — used by system jobs (non-payment, chargebacks, etc.)
     */
    @Override
    public void cancelAppointment(AppointmentStatus status, Appointment appointment, String message) {

        appointment.setAppointmentStatus(status);

        appointment.setReason_for_cancellation(message);

        appointmentRepository.save(appointment);
    }


    /**
     * Manual cancel — called from controller by secretary or dentist.
     * Uses the single CANCELLED status; the reason clarifies who cancelled.
     */
    @Override
    @Transactional
    public AppointmentCancelledResponse cancelAppointment(CancelAppointmentRequest request) {

        Appointment appointment = appointmentRepository.findByIdForCancellation(request.id_appointment())
                .orElseThrow(() -> new AppointmentNotFoundException("Appointment not found"));

        if ( appointment.isInTerminalState() ) {
            throw new AppointmentStateException("The appointment cannot be cancelled in its current state: "  + appointment.getAppointmentStatus());
        }

        TreatmentPayment payment = appointment.getPrimaryPayment();

        paymentService.actualizePaymentStatusToCancelled(payment);

        appointment.setReason_for_cancellation(request.reason_for_cancellation());

        appointment.setCancelled_at(LocalDateTime.now());

        appointment.setAppointmentStatus( AppointmentStatus.toCancelled( request.cancelledBy().name() )  );

        // The reason field already stores who cancelled and why.
        // I believe the best course of action is to email both the patient and the dentist.
        emailService.sendAppointmentManuallyCancelled(appointment);

        appointmentRepository.save(appointment);

        return appointmentMapper.toCancelledResponse(appointment);
    }


    // ── Private helpers ───────────────────────────────────────────────────────

    private void validateAppointmentAvailability(LocalDate date, LocalTime startTime, Integer duration, Long dentistId) {

        LocalDateTime startDate = date.atTime(startTime);
        LocalDateTime endDate = date.atTime(LocalTime.MAX);

        LocalTime endTime = startTime.plusMinutes(duration);

        List<Appointment> existing = appointmentRepository.findConflictingAppointments( startDate, endDate, dentistId);

        for (Appointment appo : existing) {

            if ( appo.isCancelled() ) continue;

            LocalTime existingStart = appo.getAppointmentStart().toLocalTime();

            LocalTime existingEnd = existingStart.plusMinutes(appo.getDuration_minutes());


            if ( startTime.isBefore(existingEnd) && endTime.isAfter(existingStart) ) {
                throw new AppointmentConflictException( String.format("Time slot not available. Conflict with appointment at: " + existingStart) );
            }

        }
    }

    /**
     * Marks a NO_SHOW appointment as WALK_IN_PENDING.
     * The patient arrived late and is waiting informally.
     * Staff must explicitly call admitPatient afterwards.
     */
    @Override
    @Transactional
    public AppointmentTodayResponse markAsWalkIn(Long appointmentId, String username) {

        UserProfile userProfile = userProfileService.findByAuthUserUsernameWithClinic( username );

        Appointment appointment = this.findByIdWithPatientAndTreatment( appointmentId );

        if ( !appointment.getClinic().getId().equals( userProfile.getClinic().getId() ) )
            throw new AppointmentNotFoundException("The appointment with this id was not found");

        if (!appointment.isMarkWithNoShow()) {
            throw new AppointmentStateException("Only NO_SHOW appointments can be marked as walk-in. Current: "
                                                + appointment.getAppointmentStatus());
        }

        appointment.setAppointmentStatus(AppointmentStatus.WALK_IN_PENDING);
        appointment.setLateArrival(true);
        appointmentRepository.save(appointment);

        return appointmentMapper.buildAppointmentTodayResponse(appointment);
    }

    @Override
    public void persistAppointment(Appointment appointment) {
        appointmentRepository.save(appointment);
    }
}