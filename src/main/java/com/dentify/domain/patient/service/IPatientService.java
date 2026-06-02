package com.dentify.domain.patient.service;

import com.dentify.domain.patient.dto.request.CreatePatientRequestDTO;
import com.dentify.domain.patient.dto.response.PatientResponse;
import com.dentify.domain.patient.model.Patient;

import java.time.LocalDate;
import java.util.List;

public interface IPatientService {

    public String savePatient(CreatePatientRequestDTO request, String username);

    public boolean verifyMinorAge( LocalDate dateOfBirth);

    public Patient findPatientById(Long id_patient);

    public void validatePatientEmails(List<String> email);

    public List<String> resolvePatientEmail(Patient patient);

    List<PatientResponse> findAllByClinic(String username);

    Patient findPatientByIdAndClinicId(Long patientId, Long clinicId);
}



