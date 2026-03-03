package com.dentify.domain.userProfile.repository;

import com.dentify.domain.userProfile.model.UserProfile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface IUserProfileRepository extends JpaRepository<UserProfile, Long> {

    @Query("""
           SELECT up
           FROM UserProfile up
           JOIN FETCH up.clinic
           WHERE up.authUser.username = :username
           """)
    Optional<UserProfile> findByAuthUsername(@Param("username") String username);

    @Query("""
           SELECT DISTINCT up
           FROM UserProfile up
           JOIN FETCH up.dentist d
           LEFT JOIN FETCH d.specialities
           JOIN up.authUser auth
           JOIN auth.roles r
           WHERE r.roleName = :roleName
           """)
    List<UserProfile> findAllByRoleName(@Param("roleName") String roleName);

    @Query("""
           SELECT up
           FROM UserProfile up
           JOIN FETCH up.authUser
           WHERE up.id = :id
           """)
    Optional<UserProfile> findByIdWithAuthUser(@Param("id") Long id);


    @Query("""
    SELECT up
    FROM UserProfile up
    JOIN FETCH up.clinic c
    JOIN up.authUser au
    WHERE au.username = :username
    """)
    Optional<UserProfile> findByAuthUserUsernameWithClinic(String username);
}
