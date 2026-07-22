package com.dentify.domain.dentist.repository;

import com.dentify.domain.dentist.model.Dentist;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface IDentistRepository extends JpaRepository<Dentist, Long> {

    @Query("""
        SELECT d
        FROM Dentist d
        JOIN FETCH d.userProfile up
        JOIN FETCH d.clinic c
        WHERE d.id = :id
    """)
    Optional<Dentist> findByIdWithProfileAndClinic(@Param("id") Long id);

    @Query("""
        SELECT DISTINCT d
        FROM Dentist d
        LEFT JOIN FETCH d.secretaries s
        JOIN d.clinic c
        WHERE d.id = :id
    """)
    Optional<Dentist> findByIdWithSecretaries(@Param("id") Long id);

    @EntityGraph(attributePaths = {"userProfile", "clinic", "appointments", "treatments"})
    @Query("""
        SELECT d
        FROM Dentist d
        JOIN d.userProfile up
        JOIN up.authUser au
        WHERE au.username = :username
    """)
    Optional<Dentist> findDentistByAuthUserUsername(@Param("username") String username);


    @Query("""
        SELECT d.id
        FROM Dentist d
        JOIN d.userProfile up
        JOIN up.authUser au
        WHERE au.username = :username
    """)
    Optional<Long> findDentistIdByAuthUserUsername(@Param("username") String username);
}
