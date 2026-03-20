package com.dentify.domain.appointment.controller;

import com.dentify.domain.appointment.dto.response.AppointmentTodayResponse;
import com.dentify.domain.appointment.dto.response.FullAppointmentResponse;
import com.dentify.domain.appointment.dto.request.CancelAppointmentRequest;
import com.dentify.domain.appointment.dto.request.CreateAppointmentRequestDTO;
import com.dentify.domain.appointment.dto.response.AppointmentCancelledResponse;
import com.dentify.domain.appointment.dto.response.CreateAppointmentResponseDTO;
import com.dentify.domain.appointment.service.IAppointmentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/appointments")
@Slf4j
public class AppointmentController {

    @Autowired
    private IAppointmentService appointmentService;

    @Operation( summary = "Create appointment")
    @PostMapping("/save")
    public ResponseEntity<?> saveAppointment(@RequestBody CreateAppointmentRequestDTO request){
        CreateAppointmentResponseDTO response = appointmentService.saveAppointmentWithPay(request);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Get appointment by ID")
    @ApiResponses({
                    @ApiResponse(responseCode = "200", description = "Appointment found"),
                    @ApiResponse(responseCode = "404", description = "Appointment not found")
    })
    @GetMapping("/find/{id}")
    public ResponseEntity<FullAppointmentResponse> getAppointmentById(@Parameter(description = "Appointment ID", example = "1")
                                                    @PathVariable Long id){

        FullAppointmentResponse response = appointmentService.getAppointmentById(id);
        return ResponseEntity.status(200).body(response);
    }

    @PatchMapping("/cancel")
    public ResponseEntity<AppointmentCancelledResponse> cancelAppointment(@RequestBody CancelAppointmentRequest request){

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
    public ResponseEntity<AppointmentTodayResponse> admitPatient(@PathVariable Long idAppointment){

        AppointmentTodayResponse response = appointmentService.admitPatient(idAppointment);
        return ResponseEntity.status(200).body(response);
    }
}
