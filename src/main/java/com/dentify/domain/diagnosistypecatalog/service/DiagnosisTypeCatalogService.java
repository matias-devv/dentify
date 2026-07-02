package com.dentify.domain.diagnosistypecatalog.service;

import com.dentify.domain.diagnosistypecatalog.model.DiagnosisTypeCatalog;
import com.dentify.domain.diagnosistypecatalog.repository.IDiagnosisTypeCatalogRepository;
import com.dentify.domain.toothrecord.dto.request.CreateToothRecordItem;
import com.dentify.exception.diagnosistypecatalog.DiagnosisTypeNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DiagnosisTypeCatalogService implements IDiagnosisTypeCatalogService{

    private final IDiagnosisTypeCatalogRepository diagnosisTypeCatalogRepository;

    @Override
    public Map<Long, DiagnosisTypeCatalog> resolveAccessibleDiagnoses(List<CreateToothRecordItem> toothRecordItems, Long clinicId) {

        Set<Long> requestedDiagnosisIds = toothRecordItems.stream()
                                                          .map( CreateToothRecordItem::getDiagnosisId )
                                                          .collect( Collectors.toSet() );

        Map<Long, DiagnosisTypeCatalog> accessibleDiagnoses = diagnosisTypeCatalogRepository
                                                              .findAccessibleByIds( requestedDiagnosisIds, clinicId )
                                                              .stream()
                                                              .collect( Collectors.toMap( DiagnosisTypeCatalog::getId, diagnosis -> diagnosis ) );

        Set<Long> missingDiagnosisIds = requestedDiagnosisIds.stream()
                                                             .filter(id -> !accessibleDiagnoses.containsKey( id ) )
                                                             .collect( Collectors.toSet() );

        if ( !missingDiagnosisIds.isEmpty() ) throw new DiagnosisTypeNotFoundException("Diagnosis not found, ids: " + missingDiagnosisIds);

        return accessibleDiagnoses;
    }
}
