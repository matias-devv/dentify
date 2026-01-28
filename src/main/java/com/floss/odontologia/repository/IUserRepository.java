package com.floss.odontologia.repository;

import com.floss.odontologia.model.AuthUser;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface IUserRepository extends JpaRepository<AuthUser, Long> {
}
