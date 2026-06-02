package com.dentify.domain.allergycatalog.repository;

import com.dentify.domain.allergycatalog.model.AllergyCatalog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.Set;

@Repository
public interface IAllergyCatalogRepository extends JpaRepository<AllergyCatalog, Long> {

    @Query("""
           SELECT ac
           FROM AllergyCatalog ac
           WHERE ac.id IN :ids
           AND ac.active = true
           """)
    Optional< List<AllergyCatalog> > findAllergiesWithThisIds(@Param("ids") List<Long> ids);

    @Query("SELECT a.name FROM AllergyCatalog a")
    Set<String> findAllNames();

}
