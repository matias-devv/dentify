package com.dentify.domain.appointment.controller;

import com.dentify.domain.appointment.dto.response.AppointmentTodayResponse;
import com.dentify.domain.appointment.dto.response.FullAppointmentResponse;
import com.dentify.domain.appointment.dto.request.CancelAppointmentRequest;
import com.dentify.domain.appointment.dto.request.CreateAppointmentRequestDTO;
import com.dentify.domain.appointment.dto.response.AppointmentCancelledResponse;
import com.dentify.domain.appointment.dto.response.CreateAppointmentResponseDTO;
import com.dentify.domain.appointment.service.IAppointmentService;
import com.mercadopago.net.HttpStatus;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/appointments")
@Slf4j
@RequiredArgsConstructor
public class AppointmentController {

    private final IAppointmentService appointmentService;

    @PreAuthorize("hasAnyRole('DENTIST','SECRETARY')")
    @PostMapping("/save")
    public ResponseEntity<CreateAppointmentResponseDTO> saveAppointment(@RequestBody @Valid CreateAppointmentRequestDTO request){
        return ResponseEntity.status(HttpStatus.CREATED).body(appointmentService.saveAppointmentWithPay(request));
    }

    @PreAuthorize("hasAnyRole('DENTIST','SECRETARY')")
    @GetMapping("/find/{id}")
    public ResponseEntity<FullAppointmentResponse> getAppointmentById(@Parameter(description = "Appointment ID", example = "1")
                                                    @PathVariable Long id){

        FullAppointmentResponse response = appointmentService.getAppointmentById(id);
        return ResponseEntity.status(200).body(response);
    }

    @PreAuthorize("hasAnyRole('DENTIST','SECRETARY')")
    @PatchMapping("/cancel")
    public ResponseEntity<AppointmentCancelledResponse> cancelAppointment(@RequestBody @Valid CancelAppointmentRequest request){

        AppointmentCancelledResponse response = appointmentService.cancelAppointment(request);
        return ResponseEntity.status(200).body(response);
    }

    @PreAuthorize("hasRole('DENTIST')")
    @GetMapping("/today")
    public ResponseEntity<List> getAppointmentsTodayForDentist(@AuthenticationPrincipal String username){

        List<AppointmentTodayResponse> response = appointmentService.getAppointmentsTodayForDentist(username);
        return ResponseEntity.status(200).body(response);
    }

    @PreAuthorize("hasAnyRole('DENTIST', 'SECRETARY')")
    @PatchMapping("/admit/{idAppointment}")
    public ResponseEntity<AppointmentTodayResponse> admitPatient(@PathVariable Long idAppointment,
                                                                 @AuthenticationPrincipal String username){

        AppointmentTodayResponse response = appointmentService.admitPatient(idAppointment, username);
        return ResponseEntity.status(200).body(response);
    }

    @PreAuthorize("hasAnyRole('DENTIST', 'SECRETARY')")
    @PatchMapping("/start-attention/{idAppointment}")
    public ResponseEntity<AppointmentTodayResponse> markPatientInAttention(@PathVariable Long idAppointment,
                                                                           @AuthenticationPrincipal String username){

        AppointmentTodayResponse response = appointmentService.startAttention(idAppointment, username);
        return ResponseEntity.status(200).body(response);
    }

    @PreAuthorize("hasAnyRole('DENTIST', 'SECRETARY')")
    @PatchMapping("/complete/{idAppointment}")
    public ResponseEntity<AppointmentTodayResponse> markAppointmentCompleted(@PathVariable Long idAppointment,
                                                                             @AuthenticationPrincipal String username){

        AppointmentTodayResponse response = appointmentService.completeAppointment(idAppointment, username);
        return ResponseEntity.status(200).body(response);
    }

    @PreAuthorize("hasAnyRole('DENTIST', 'SECRETARY')")
    @PatchMapping("/walk-in/{idAppointment}")
    public ResponseEntity<AppointmentTodayResponse> markAsWalkIn(@PathVariable Long idAppointment,
                                                                 @AuthenticationPrincipal String username) {
        return ResponseEntity.ok(appointmentService.markAsWalkIn(idAppointment, username));
    }
}
