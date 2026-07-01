package com.dentify.mapper;

import com.dentify.domain.diagnosistypecatalog.dto.response.DiagnosisTypeCatalogResponse;
import com.dentify.domain.diagnosistypecatalog.model.DiagnosisTypeCatalog;
import com.dentify.domain.medicalhistory.model.MedicalHistory;
import com.dentify.domain.toothrecord.dto.request.CreateToothRecordItem;
import com.dentify.domain.toothrecord.dto.response.ToothRecordResponse;
import com.dentify.domain.toothrecord.model.ToothRecord;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

@Component
public class ToothRecordMapper {

    public List<ToothRecord> buildToothRecordList( List<CreateToothRecordItem> toothRecordItems, Map<Long, DiagnosisTypeCatalog> resolvedDiagnoses,
                                                   MedicalHistory medicalHistory ) {

        List<ToothRecord> records = new ArrayList<>();

        for (CreateToothRecordItem item : toothRecordItems) {

            DiagnosisTypeCatalog diagnosis = resolvedDiagnoses.get( item.getDiagnosisId() );

            for ( Integer pieceNumber : item.getPieceNumbers() ) {

                ToothRecord record = ToothRecord.builder()
                                                .pieceNumber(pieceNumber)
                                                .recordType(item.getRecordType())
                                                .face(item.getFace())
                                                .observations(item.getObservations())
                                                .diagnosisType(diagnosis)
                                                .medicalHistory(medicalHistory)
                                                .build();
                records.add(record);
            }
        }
        return records;
    }

    public ToothRecordResponse toResponse(ToothRecord record) {

        if (record == null) {
            return null;
        }

        DiagnosisTypeCatalogResponse diagnosisResponse = ( record.getDiagnosisType() != null ) ? this.buildTypeCatalogResponse( record.getDiagnosisType() ) : null;

        return new ToothRecordResponse( record.getId(),
                                        record.getPieceNumber(),
                                        record.getRecordType().name(),
                                        record.getFace().name(),
                                        record.getObservations(),
                                        record.getCreatedAt().toString(),
                                        diagnosisResponse );
    }

    public List<ToothRecordResponse> toResponseList(List<ToothRecord> records) {

        if (records == null) {
            return Collections.emptyList();
        }

        List<ToothRecordResponse> responseList = new ArrayList<>(records.size());

        for (ToothRecord record : records) {
            responseList.add(toResponse(record));
        }

        return responseList;
    }

    private DiagnosisTypeCatalogResponse buildTypeCatalogResponse(DiagnosisTypeCatalog catalog) {
        return new DiagnosisTypeCatalogResponse( catalog.getId(),
                                                 catalog.getName(),
                                                 catalog.getSymbol().name(),
                                                 catalog.getIsGlobal(),
                                                 catalog.getActive() );
    }

}
