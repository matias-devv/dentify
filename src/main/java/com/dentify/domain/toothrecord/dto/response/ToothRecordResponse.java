package com.dentify.domain.toothrecord.dto.response;

import com.dentify.domain.diagnosistypecatalog.dto.response.DiagnosisTypeCatalogResponse;

public record ToothRecordResponse(Long id,
                                   int pieceNumber,
                                   String recordType,
                                   String toothFace,
                                   String observations,
                                   String createdAt,
                                   DiagnosisTypeCatalogResponse diagnosis){}
