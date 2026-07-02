package com.dentify.domain.diagnosistypecatalog.repository;

import com.dentify.domain.diagnosistypecatalog.model.DiagnosisTypeCatalog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Set;

@Repository
public interface IDiagnosisTypeCatalogRepository extends JpaRepository<DiagnosisTypeCatalog, Long> {

    @Query("""
        SELECT d
        FROM DiagnosisTypeCatalog d
        WHERE d.id IN :ids
          AND d.active = true
          AND (
                d.isGlobal = true
                OR d.clinic.id = :clinicId
          )
    """)
    List<DiagnosisTypeCatalog> findAccessibleByIds(@Param("ids") Set<Long> ids, @Param("clinicId") Long clinicId);
}
