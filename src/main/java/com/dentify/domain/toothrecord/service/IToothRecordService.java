package com.dentify.domain.toothrecord.service;

import com.dentify.domain.diagnosistypecatalog.exception.DiagnosisTypeNotFoundException;
import com.dentify.domain.toothrecord.dto.request.CreateToothRecordItem;
import com.dentify.domain.toothrecord.enums.OdontogramType;
import com.dentify.exception.toothrecord.InvalidPieceNumberException;
import com.dentify.domain.toothrecord.model.ToothRecord;
import com.dentify.domain.medicalhistory.model.MedicalHistory;

import java.util.List;

public interface IToothRecordService {

    /**
     * Builds and validates tooth records for a medical history from raw create requests.
     * <p>
     * The method assumes that `toothRecordItems` is non-null and non-empty when invoked.
     * This responsibility lies with the caller (see Task 06), which must skip the call
     * for null or empty input.
     * <p>
     * For each item in `toothRecordItems`, the method creates one or more `ToothRecord`
     * entities by expanding the `piece_numbers` field, validating that each piece number
     * conforms to the `odontogram_type` ranges:
     * <ul>
     *   <li>`ADULT`: 11–18 / 21–28 / 31–38 / 41–48</li>
     *   <li>`PEDIATRIC`: 51–55 / 61–65 / 71–75 / 81–85</li>
     *   <li>`MIX`: union of both ranges</li>
     * </ul>
     * <p>
     * Each tooth record is validated for a resolvable `DiagnosisTypeCatalog` entry,
     * where non-global entries must belong to the authenticated dentist's `clinic_id`.
     * <p>
     * The constructed records receive the back‑reference to their parent
     * `MedicalHistory` aggregate and are returned with `diagnosisType` already
     * resolved. The caller is responsible for assigning the returned list to
     * `medicalHistory.toothRecords` and executing a single `save()` call.
     *
     * @param toothRecordItems       raw tooth‑record items from {@code CreateMedicalHistoryRequest};
     *                               may be {@code null} or empty, but the implementation assumes
     *                               a non‑empty, non‑null list when invoked
     * @param odontogramType         the odontogram type of the parent request, defining the
     *                               valid FDI piece‑number ranges
     * @param clinicId               the clinic ID of the authenticated dentist, used to validate
     *                               accessibility of non‑global diagnosis entries
     * @param medicalHistory        the not‑yet‑persisted {@code MedicalHistory} aggregate being built;
     *                               used only to establish the back‑reference on each tooth record
     * @return                      fully validated and constructed list of tooth records, one per
     *                               expanded piece number with back‑reference and resolved diagnosis type
     * @throws DiagnosisTypeNotFoundException if a referenced diagnosis type cannot be resolved
     * @throws InvalidPieceNumberException if any piece number falls outside the valid FDI range
     */
    List<ToothRecord> buildToothRecordsForMedicalHistory(List<CreateToothRecordItem> toothRecordItems, OdontogramType odontogramType,
                                                         Long clinicId, MedicalHistory medicalHistory);

}
