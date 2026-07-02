package com.dentify.domain.diagnosistypecatalog.dto.response;

public record DiagnosisTypeCatalogResponse(Long id,
                                            String name,
                                            String symbol,
                                            Boolean isGlobal,
                                            Boolean active){}
