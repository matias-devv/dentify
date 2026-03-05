package com.dentify.security.repository;

import com.dentify.security.model.AuthUser;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface IAuthUserRepository extends JpaRepository<AuthUser, Long> {

    @Query("SELECT au FROM AuthUser au LEFT JOIN FETCH au.roles WHERE au.username = :username")
    Optional<AuthUser> findByUsernameWithRoles(@Param("username") String username);

    boolean existsByUsername(String email);

    @Query("""
       SELECT au
       FROM AuthUser au
       LEFT JOIN FETCH au.userProfile up
       LEFT JOIN FETCH up.clinic
       WHERE au.username = :username
       """)
    Optional<AuthUser> findByUsernameWithProfileAndClinic(@Param("username") String username);
}
