package com.dentify.domain.diagnosistypecatalog.service;

import com.dentify.domain.diagnosistypecatalog.model.DiagnosisTypeCatalog;
import com.dentify.domain.toothrecord.dto.request.CreateToothRecordItem;

import java.util.List;
import java.util.Map;

public interface IDiagnosisTypeCatalogService {

    public Map<Long, DiagnosisTypeCatalog> resolveAccessibleDiagnoses(List<CreateToothRecordItem> toothRecordItems, Long clinicId);
}
