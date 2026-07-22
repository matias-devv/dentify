package com.dentify.domain.toothrecord.service;

import com.dentify.domain.diagnosistypecatalog.dto.request.PieceDiagnosisKey;
import com.dentify.domain.diagnosistypecatalog.model.DiagnosisTypeCatalog;
import com.dentify.domain.diagnosistypecatalog.service.IDiagnosisTypeCatalogService;
import com.dentify.domain.medicalhistory.model.MedicalHistory;
import com.dentify.domain.toothrecord.dto.request.CreateToothRecordItem;
import com.dentify.domain.toothrecord.dto.response.ToothRecordResponse;
import com.dentify.domain.toothrecord.enums.OdontogramType;
import com.dentify.domain.toothrecord.enums.ToothFace;
import com.dentify.domain.toothrecord.model.ToothRecord;
import com.dentify.exception.diagnosistypecatalog.DiagnosisTypeNotFoundException;
import com.dentify.exception.toothrecord.DuplicateToothRecordException;
import com.dentify.exception.toothrecord.InvalidPieceNumberException;
import com.dentify.exception.toothrecord.ToothRecordFaceConflictException;
import com.dentify.mapper.ToothRecordMapper;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ToothRecordService implements IToothRecordService {

    private static final Set<Integer> ADULT_PIECE_NUMBERS = rangeUnion(11, 18, 21, 28, 31, 38, 41, 48);

    private static final Set<Integer> PEDIATRIC_PIECE_NUMBERS = rangeUnion(51, 55, 61, 65, 71, 75, 81, 85);

    private static final Set<Integer> MIX_PIECE_NUMBERS = union(ADULT_PIECE_NUMBERS, PEDIATRIC_PIECE_NUMBERS);

    //services
    private final IDiagnosisTypeCatalogService diagnosisTypeCatalogService;

    //mappers
    private final ToothRecordMapper toothRecordMapper;

    /**
     * Validates and builds the {@link ToothRecord} entities for the optional {@code tooth_records}
     *
     * <p> No entity is constructed until every item passes all three checks.
     *
     * @throws DiagnosisTypeNotFoundException if any {@code diagnosisId} is missing, inactive, or inaccessible for {@code clinicId}
     * @throws InvalidPieceNumberException if any piece number falls outside the FDI range for {@code odontogramType}
     */
    @Override
    public List<ToothRecord> processToothRecords(List<CreateToothRecordItem> toothRecordItems, OdontogramType odontogramType, Long clinicId, MedicalHistory medicalHistory) {

        this.validatePieceNumbersPresence( toothRecordItems );

        this.validateToothRecordUniqueness( medicalHistory.getToothRecords(), toothRecordItems );

        this.validatePieceNumberRanges( toothRecordItems, odontogramType );

        Set<Long> requestedDiagnosisIds = this.getRequestedDiagnosisIds( toothRecordItems );

        Map<Long, DiagnosisTypeCatalog> resolvedDiagnoses = diagnosisTypeCatalogService.resolveAccessibleDiagnoses( requestedDiagnosisIds, clinicId );

        return toothRecordMapper.buildToothRecordList(toothRecordItems, resolvedDiagnoses, medicalHistory);
    }

    private void validatePieceNumbersPresence(List<CreateToothRecordItem> toothRecordItems) {

        boolean hasEmptyPieceNumbers = toothRecordItems.stream()
                                                       .map( CreateToothRecordItem::getPieceNumbers )
                                                       .anyMatch( pieceNumbers -> pieceNumbers == null || pieceNumbers.isEmpty() );

        if ( hasEmptyPieceNumbers ) throw new InvalidPieceNumberException("Each tooth record item must declare at least one piece number.");
    }

    /**
     * Uniqueness key: (pieceNumber, face, diagnosisType). Additionally, {@code WHOLE_TOOTH} is treated as occupying the entire piece for a given diagnosis
     * it cannot coexist with any other face for the same (pieceNumber, diagnosisType).
     *
     * <p><b>Assumption:</b> the current entities don't encode this WHOLE_TOOTH/partial-face relationship explicitly; this method is the single place enforcing it,
     * checked against both already-persisted records and the incoming bulk request together.
     *
     * @throws DuplicateToothRecordException if an identical (pieceNumber, face, diagnosisId) tuple already exists, either persisted or earlier in the same request
     *
     * @throws ToothRecordFaceConflictException if a WHOLE_TOOTH entry conflicts with a partial-face entry for the same (pieceNumber, diagnosisId), in either direction
     */
    private void validateToothRecordUniqueness( List<ToothRecord> persistedToothRecords, List<CreateToothRecordItem> toothRecordItems) {

        Map<PieceDiagnosisKey, Set<ToothFace> > facesByPieceAndDiagnosis = this.buildFacesByPieceAndDiagnosisMap(persistedToothRecords);

        for ( CreateToothRecordItem item : toothRecordItems ) {

            for ( Integer pieceNumber : item.getPieceNumbers() ) {

                PieceDiagnosisKey key = new PieceDiagnosisKey( pieceNumber, item.getDiagnosisId() );

                // if already exists the key from the database/from the request, e.g. : { pieceNumber = 11, diagnosisId = 1 } and the request is something like: { pieceNumber = 11, diagnosisId = 1 }
                // then returns a Set charged with one or more faces, but if some value from the key is different, the Set is new -> empty
                Set<ToothFace> occupiedFaces = facesByPieceAndDiagnosis.computeIfAbsent( key, k -> new HashSet<>() );

                this.verifyExactlyDuplicateToothRecord( occupiedFaces, item.getFace(), pieceNumber, item.getDiagnosisId()  );

                this.verifyPartialFaceAgainstExistingWholeTooth( item, occupiedFaces, pieceNumber );

                this.verifyWholeToothAgainstExistingPartialFaces( item, occupiedFaces, pieceNumber );

                occupiedFaces.add( item.getFace() );
            }
        }
    }

    private Map< PieceDiagnosisKey, Set<ToothFace> > buildFacesByPieceAndDiagnosisMap(List<ToothRecord> persistedToothRecords) {

        Map< PieceDiagnosisKey, Set<ToothFace> > facesByPieceAndDiagnosis = new HashMap<>();

        for (ToothRecord persisted : persistedToothRecords) {

            PieceDiagnosisKey key = new PieceDiagnosisKey( persisted.getPieceNumber(), persisted.getDiagnosisType().getId() );

            facesByPieceAndDiagnosis.computeIfAbsent( key, k -> new HashSet<>() )
                                    .add( persisted.getFace() );
        }
        return facesByPieceAndDiagnosis;
    }

    private void verifyExactlyDuplicateToothRecord( Set<ToothFace> occupiedFaces, ToothFace face, int pieceNumber, Long diagnosisId ) {

        if ( occupiedFaces.contains( face ) ) {
            throw new DuplicateToothRecordException( "Duplicate tooth record: pieceNumber=" + pieceNumber + ", face=" + face  + ", diagnosisId=" + diagnosisId );
        }
    }

    private void verifyPartialFaceAgainstExistingWholeTooth(CreateToothRecordItem item, Set<ToothFace> occupiedFaces, int pieceNumber) {

        if ( item.isWholeTooth() && !occupiedFaces.isEmpty() ) {

            throw new ToothRecordFaceConflictException( "Piece " + pieceNumber + " already has partial-face records for diagnosis " + item.getDiagnosisId() +
                                                        "; cannot add a whole tooth record for the same diagnosis.");
        }
    }

    private void verifyWholeToothAgainstExistingPartialFaces(CreateToothRecordItem item, Set<ToothFace> occupiedFaces, int pieceNumber) {

        boolean pieceAlreadyHasWholeTooth = occupiedFaces.contains(ToothFace.WHOLE_TOOTH);

        if ( !item.isWholeTooth() && pieceAlreadyHasWholeTooth ) {

            throw new ToothRecordFaceConflictException( "Piece " + pieceNumber + " already has a WHOLE_TOOTH record for diagnosis " + item.getDiagnosisId() +
                                                        "; cannot add a partial face record for the same diagnosis.");
        }
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

    private Set<Long> getRequestedDiagnosisIds(List<CreateToothRecordItem> toothRecordItems) {

        return toothRecordItems.stream()
                               .map( CreateToothRecordItem::getDiagnosisId )
                               .collect( Collectors.toSet() );
    }

    @Override
    public List<ToothRecordResponse> toResponseList(List<ToothRecord> toothRecords) {
        return toothRecordMapper.toResponseList(toothRecords);
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