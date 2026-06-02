package com.dentify.domain.medicalhistory.controller;

import com.dentify.domain.medicalhistory.dto.request.CreateMedicalHistoryRequest;
import com.dentify.domain.medicalhistory.dto.response.CreateMedicalHistoryResponse;
import com.dentify.domain.medicalhistory.dto.response.MedicalHistorySummaryResponse;
import com.dentify.domain.medicalhistory.service.IMedicalHistoryService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/medical-histories")
@RequiredArgsConstructor
public class MedicalHistoryController {

    private final IMedicalHistoryService medicalHistoryService;

    @PreAuthorize("hasRole('DENTIST')")
    @PostMapping("")
    public ResponseEntity<CreateMedicalHistoryResponse> createMedicalHistory(@RequestParam @NotNull Long patientId,
                                                                             @AuthenticationPrincipal String username,
                                                                             @Valid @RequestBody CreateMedicalHistoryRequest request){

        return ResponseEntity.status(HttpStatus.CREATED).body( medicalHistoryService.createMedicalHistory( patientId, username, request ) );
    }

    @PreAuthorize("hasAnyRole('DENTIST','SECRETARY')")
    @GetMapping("/find-all/{patientId}")
    public ResponseEntity<List> findAllClinicalRecords(@PathVariable Long patientId) {

        List<MedicalHistorySummaryResponse> records = medicalHistoryService.findAllByPatient(patientId);

        return ResponseEntity.ok(records);
    }
}
