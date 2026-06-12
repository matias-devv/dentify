package com.dentify.domain.medicalhistory.service;

import com.dentify.domain.medicalhistory.dto.request.CreateMedicalHistoryRequest;
import com.dentify.domain.medicalhistory.dto.response.CreateMedicalHistoryResponse;
import com.dentify.domain.medicalhistory.dto.response.MedicalHistoryDetailResponse;
import com.dentify.domain.medicalhistory.dto.response.MedicalHistorySummaryResponse;

import java.util.List;

public interface IMedicalHistoryService {

    public CreateMedicalHistoryResponse createMedicalHistory(Long patientId, String username, CreateMedicalHistoryRequest request);

    public List<MedicalHistorySummaryResponse> findAllByPatient(Long patientId);

    public MedicalHistoryDetailResponse getMedicalHistoryDetail(Long patientId, Long medicalHistoryId, String username);
}
