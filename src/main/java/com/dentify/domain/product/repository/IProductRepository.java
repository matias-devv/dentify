package com.dentify.domain.product.repository;

import com.dentify.domain.clinic.model.Clinic;
import com.dentify.domain.product.model.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.Set;

@Repository
public interface IProductRepository extends JpaRepository<Product, Long> {

    @Query("""
    SELECT p FROM Product p
    JOIN FETCH p.speciality s
    WHERE p.active = true AND p.clinic = :clinic
    """)
    List<Product> findAllActiveWithSpeciality(@Param("clinic") Clinic clinic);

    @Query("""
    SELECT p
    FROM Product p
    JOIN p.clinic c
    WHERE p.id_product = :productId
    """)
    Optional<Product> findProductById(@Param("productId") Long productId);

    @Query("""
    SELECT CASE
            WHEN COUNT(p) > 0 THEN true
            ELSE false
        END
        FROM Product p
        WHERE p.nameProduct = :nameProduct
          AND p.clinic.id_clinic = :clinicId
    """)
    boolean existsByNameAndClinicId(@Param("nameProduct") String nameProduct, @Param("clinicId") Long clinicId);

    @Query("""
    SELECT p FROM Product p WHERE p.nameProduct IN :names AND p.clinic = :clinic
    """)
    List<Product> findProductNameAndIdByNamesAndClinic(@Param("names") Set<String> names, @Param("clinic") Clinic clinic);
}
