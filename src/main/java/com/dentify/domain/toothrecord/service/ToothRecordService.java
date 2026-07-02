package com.dentify.domain.toothrecord.service;

import com.dentify.domain.diagnosistypecatalog.model.DiagnosisTypeCatalog;
import com.dentify.domain.diagnosistypecatalog.service.IDiagnosisTypeCatalogService;
import com.dentify.domain.medicalhistory.model.MedicalHistory;
import com.dentify.domain.toothrecord.dto.request.CreateToothRecordItem;
import com.dentify.domain.toothrecord.enums.OdontogramType;
import com.dentify.domain.toothrecord.model.ToothRecord;
import com.dentify.exception.diagnosistypecatalog.DiagnosisTypeNotFoundException;
import com.dentify.exception.toothrecord.InvalidPieceNumberException;
import com.dentify.mapper.ToothRecordMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
/**
 * NOTE: this class already implements {@link IToothRecordService} for the standalone
 * {@code POST /medical-histories/{medicalHistoryId}/tooth-records} endpoint. The members below
 * are the additions required by task 02 — merge them into the existing class body without
 * altering any pre-existing method, field, or constructor parameter used by that endpoint.
 */
@Service
@RequiredArgsConstructor
public class ToothRecordService implements IToothRecordService {

    private static final Set<Integer> ADULT_PIECE_NUMBERS = rangeUnion(11, 18, 21, 28, 31, 38, 41, 48);

    private static final Set<Integer> PEDIATRIC_PIECE_NUMBERS = rangeUnion(51, 55, 61, 65, 71, 75, 81, 85);

    private static final Set<Integer> MIX_PIECE_NUMBERS = union(ADULT_PIECE_NUMBERS, PEDIATRIC_PIECE_NUMBERS);

    private final IDiagnosisTypeCatalogService diagnosisTypeCatalogService;
    private final ToothRecordMapper toothRecordMapper;

    /**
     * Validates and builds the {@link ToothRecord} entities for the optional {@code tooth_records}
     *
     * <p>Validation order is intentional: presence, then diagnosis accessibility (bulk), then
     * piece-number range. No entity is constructed until every item passes all three checks.
     *
     * @throws DiagnosisTypeNotFoundException if any {@code diagnosisId} is missing, inactive, or inaccessible for {@code clinicId}
     * @throws InvalidPieceNumberException if any piece number falls outside the FDI range for {@code odontogramType}
     */
    @Override
    public List<ToothRecord> buildToothRecordsForMedicalHistory(List<CreateToothRecordItem> toothRecordItems, OdontogramType odontogramType, Long clinicId,
                                                                MedicalHistory medicalHistory) {

        this.validatePieceNumbersPresence(toothRecordItems);

        Map<Long, DiagnosisTypeCatalog> resolvedDiagnoses = diagnosisTypeCatalogService.resolveAccessibleDiagnoses( toothRecordItems, clinicId );

        this.validatePieceNumberRanges( toothRecordItems, odontogramType );

        return toothRecordMapper.buildToothRecordList(toothRecordItems, resolvedDiagnoses, medicalHistory);
    }

    private void validatePieceNumbersPresence(List<CreateToothRecordItem> toothRecordItems) {

        boolean hasEmptyPieceNumbers = toothRecordItems.stream()
                                                       .map( CreateToothRecordItem::getPieceNumbers )
                                                       .anyMatch( pieceNumbers -> pieceNumbers == null || pieceNumbers.isEmpty() );

        if ( hasEmptyPieceNumbers ) throw new InvalidPieceNumberException("Each tooth record item must declare at least one piece number.");
    }

    private void validatePieceNumberRanges( List<CreateToothRecordItem> toothRecordItems, OdontogramType odontogramType ) {

        Set<Integer> validPieceNumbers = this.validPieceNumbersFor( odontogramType );

        for ( CreateToothRecordItem item : toothRecordItems ) {

            for ( Integer pieceNumber : item.getPieceNumbers() ) {

                if ( !validPieceNumbers.contains(pieceNumber) ) throw new InvalidPieceNumberException("Invalid piece number: "  + pieceNumber);
            }
        }
    }

    private Set<Integer> validPieceNumbersFor( OdontogramType odontogramType ) {
        return switch (odontogramType) {
                                         case ADULT -> ADULT_PIECE_NUMBERS;
                                         case PEDIATRIC -> PEDIATRIC_PIECE_NUMBERS;
                                         case MIX -> MIX_PIECE_NUMBERS;
        };
    }
                                            //this is like: passing a vector like a param
    private static Set<Integer> rangeUnion(int... boundsPairs) {

        Set<Integer> pieceNumbers = new HashSet<>();

        //go through a pair of numbers, and also the numbers inside the range,
        //example: for (1): range 1 { 11, 18 }    ->  for (2): 11, 12, 13, 14, 15, 16, 17, 18

        for (int i = 0; i < boundsPairs.length; i += 2) {

            for ( int piece = boundsPairs[i]; piece <= boundsPairs[i + 1]; piece++) {
                pieceNumbers.add(piece);
            }
        }
        return Collections.unmodifiableSet(pieceNumbers); // the collection converts to only be readable
    }

    private static Set<Integer> union(Set<Integer> first, Set<Integer> second) {

        Set<Integer> merged = new HashSet<>(first);

        merged.addAll(second);

        return Collections.unmodifiableSet(merged);
    }
}