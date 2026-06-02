package com.dentify.domain.medicalhistory.service;

import com.dentify.domain.medicalhistory.dto.request.CreateMedicalHistoryRequest;
import com.dentify.domain.medicalhistory.dto.response.CreateMedicalHistoryResponse;

public interface IMedicalHistoryService {

    public CreateMedicalHistoryResponse createMedicalHistory(Long patientId, String username, CreateMedicalHistoryRequest request);
}
