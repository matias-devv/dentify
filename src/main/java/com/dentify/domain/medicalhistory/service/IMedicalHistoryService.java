package com.dentify.domain.medicalhistory.service;

import com.dentify.domain.medicalhistory.dto.request.CreateMedicalHistoryRequest;
import com.dentify.domain.medicalhistory.dto.request.EditMedicalHistoryRequest;
import com.dentify.domain.medicalhistory.dto.response.CreateMedicalHistoryResponse;
import com.dentify.domain.medicalhistory.dto.response.EditMedicalHistoryResponse;
import com.dentify.domain.medicalhistory.dto.response.MedicalHistoryDetailResponse;
import com.dentify.domain.medicalhistory.dto.response.MedicalHistorySummaryResponse;
import com.dentify.domain.toothrecord.dto.request.AddToothRecordsRequest;
import com.dentify.domain.toothrecord.dto.response.ToothRecordResponse;
import jakarta.validation.Valid;

import java.util.List;

public interface IMedicalHistoryService {

    public CreateMedicalHistoryResponse createMedicalHistory(Long patientId, String username, CreateMedicalHistoryRequest request);

    public List<MedicalHistorySummaryResponse> findAllByPatient(Long patientId);

    public MedicalHistoryDetailResponse getMedicalHistoryDetail(Long patientId, Long medicalHistoryId, String username);

    public EditMedicalHistoryResponse updateMedicalHistory(EditMedicalHistoryRequest request, String username, Long patientId, Long medicalHistoryId);

    public List<ToothRecordResponse> addToothRecordsToMedicalHistory(@Valid AddToothRecordsRequest request, Long medicalHistoryId, String username);
}
