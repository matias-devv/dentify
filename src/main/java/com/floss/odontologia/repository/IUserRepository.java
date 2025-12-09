package com.floss.odontologia.repository;

import com.floss.odontologia.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface IUserRepository extends JpaRepository< User , Long> {
}
