package com.dentify.domain.toothrecord.service;

import com.dentify.domain.diagnosistypecatalog.exception.DiagnosisTypeNotFoundException;
import com.dentify.domain.diagnosistypecatalog.model.DiagnosisTypeCatalog;
import com.dentify.domain.toothrecord.dto.request.CreateToothRecordItem;
import com.dentify.exception.toothrecord.InvalidPieceNumberException;
import com.dentify.domain.toothrecord.enums.OdontogramType;
import com.dentify.domain.toothrecord.model.ToothRecord;
import com.dentify.domain.medicalhistory.model.MedicalHistory;
import com.dentify.mapper.ToothRecordMapper;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;
import java.util.Map;
import java.util.HashSet;
import java.util.stream.Collectors;

@Service
public class ToothRecordService implements IToothRecordService {

    private final ToothRecordMapper mapper;

    public ToothRecordService(ToothRecordMapper mapper) {
        this.mapper = mapper;
    }

    @Override
    public List<ToothRecord> buildToothRecordsForMedicalHistory(
        List<CreateToothRecordItem> toothRecordItems,
        OdontogramType odontogramType,
        Long clinicId,
        MedicalHistory medicalHistory
    ) {
        validatePieceNumbersNotNullOrEmpty(toothRecordItems);

        Map<Long, DiagnosisTypeCatalog> accessibleDiagnoses = resolveAccessibleDiagnoses(toothRecordItems, clinicId);

        validatePieceNumbers(toothRecordItems, odontogramType);

        return mapper.buildToothRecordList(toothRecordItems, accessibleDiagnoses, medicalHistory);
    }

    private void validatePieceNumbersNotNullOrEmpty(List<CreateToothRecordItem> items) {
        for (CreateToothRecordItem item : items) {
            if (item.getPieceNumbers() == null || item.getPieceNumbers().isEmpty()) {
                throw new IllegalArgumentException(
                    "piece_numbers must not be null or empty for diagnosis_id: " + item.getDiagnosisId()
                );
            }
        }
    }

    private Map<Long, DiagnosisTypeCatalog> resolveAccessibleDiagnoses(
            List<CreateToothRecordItem> items, Long clinicId) {

        Set<Long> diagnosisIds = items.stream()
            .map(CreateToothRecordItem::getDiagnosisId)
            .collect(Collectors.toSet());

        List<DiagnosisTypeCatalog> allAccessible = findAllByIdInAndActive(diagnosisIds);

        Map<Long, DiagnosisTypeCatalog> accessibleMap = allAccessible.stream()
            .filter(diagnosis -> isAccessible(diagnosis, clinicId))
            .collect(Collectors.toMap(DiagnosisTypeCatalog::getId, diagnosis -> diagnosis));

        for (Long diagnosisId : diagnosisIds) {
            if (!accessibleMap.containsKey(diagnosisId)) {
                throw new DiagnosisTypeNotFoundException(
                    "Diagnosis ID " + diagnosisId + " not found, inactive, or inaccessible"
                );
            }
        }

        return accessibleMap;
    }

    private List<DiagnosisTypeCatalog> findAllByIdInAndActive(Set<Long> diagnosisIds) {
        if (diagnosisIds.isEmpty()) {
            return List.of();
        }

        return null;
    }

    private boolean isAccessible(DiagnosisTypeCatalog diagnosis, Long clinicId) {
        if (!diagnosis.getActive()) {
            return false;
        }

        if (diagnosis.getIsGlobal()) {
            return true;
        }

        if (diagnosis.getClinic() == null) {
            return false;
        }

        return diagnosis.getClinic().getId().equals(clinicId);
    }

    private void validatePieceNumbers(
        List<CreateToothRecordItem> items, OdontogramType odontogramType) {

        Set<Integer> validRange = getValidPieceNumberRange(odontogramType);

        for (CreateToothRecordItem item : items) {
            for (Integer pieceNumber : item.getPieceNumbers()) {
                if (!validRange.contains(pieceNumber)) {
                    throw new InvalidPieceNumberException(
                        "Piece number " + pieceNumber + " is invalid for odontogram type " + odontogramType
                    );
                }
            }
        }
    }

    private Set<Integer> getValidPieceNumberRange(OdontogramType odontogramType) {
        return switch (odontogramType) {
            case ADULT -> Set.of(11, 12, 13, 14, 15, 16, 17, 18,
                                21, 22, 23, 24, 25, 26, 27, 28,
                                31, 32, 33, 34, 35, 36, 37, 38,
                                41, 42, 43, 44, 45, 46, 47, 48);
            case PEDIATRIC -> Set.of(51, 52, 53, 54, 55,
                                   61, 62, 63, 64, 65,
                                   71, 72, 73, 74, 75,
                                   81, 82, 83, 84, 85);
            case MIX -> {
                Set<Integer> mix = new HashSet<>();
                mix.addAll(Set.of(11, 12, 13, 14, 15, 16, 17, 18,
                                21, 22, 23, 24, 25, 26, 27, 28,
                                31, 32, 33, 34, 35, 36, 37, 38,
                                41, 42, 43, 44, 45, 46, 47, 48));
                mix.addAll(Set.of(51, 52, 53, 54, 55,
                                61, 62, 63, 64, 65,
                                71, 72, 73, 74, 75,
                                81, 82, 83, 84, 85));
                yield mix;
            }
        };
    }
}
