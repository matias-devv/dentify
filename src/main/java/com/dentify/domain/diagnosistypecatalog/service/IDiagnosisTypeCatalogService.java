package com.dentify.domain.diagnosistypecatalog.service;

import com.dentify.domain.diagnosistypecatalog.dto.response.DiagnosisTypeCatalogResponse;
import com.dentify.domain.diagnosistypecatalog.model.DiagnosisTypeCatalog;
import com.dentify.domain.toothrecord.dto.request.CreateToothRecordItem;
import org.jspecify.annotations.Nullable;

import java.util.List;
import java.util.Map;
import java.util.Set;

public interface IDiagnosisTypeCatalogService {

    public Map<Long, DiagnosisTypeCatalog> resolveAccessibleDiagnoses(  Set<Long> requestedDiagnosisIds, Long clinicId);

    /**
     * Seeds the global (system-predefined) {@link DiagnosisTypeCatalog} entries, one per
     * {@link com.dentify.domain.diagnosistypecatalog.enums.DiagnosisSymbol} value except
     * {@code CUSTOM}, which is reserved exclusively for clinic-defined entries and is never
     * created by the system.
     * <p>
     * Idempotent by design: entries are matched by {@code symbol} among existing
     * {@code isGlobal = true} records, so re-running this method on every application
     * startup only creates whatever is missing and never duplicates or overwrites
     * existing rows — including ones a clinic admin may have deactivated.
     *
     * @return the number of new global entries created in this invocation (0 if the
     *         catalog was already fully seeded)
     */
    int seedDiagnosisTypeCatalog();

    List<DiagnosisTypeCatalogResponse> listAccessibleDiagnosisTypes(String username);
}
