package com.dentify.mapper;

import com.dentify.domain.diagnosistypecatalog.dto.response.DiagnosisTypeCatalogResponse;
import com.dentify.domain.diagnosistypecatalog.model.DiagnosisTypeCatalog;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Component
public class DiagnosisTypeCatalogMapper {

    public DiagnosisTypeCatalogResponse buildTypeCatalogResponse(DiagnosisTypeCatalog catalog) {
        return new DiagnosisTypeCatalogResponse( catalog.getId(),
                                                 catalog.getName(),
                                                 catalog.getSymbol().name(),
                                                 catalog.getIsGlobal(),
                                                 catalog.getActive() );
    }

    public List<DiagnosisTypeCatalogResponse> toResponseList(List<DiagnosisTypeCatalog> accessibleDiagnosis) {

        if (accessibleDiagnosis == null) return Collections.emptyList();

        List<DiagnosisTypeCatalogResponse> responseList = new ArrayList<>(accessibleDiagnosis.size());

        accessibleDiagnosis.forEach(entry -> responseList.add( this.buildTypeCatalogResponse(entry) ) );

        return responseList;
    }
}
