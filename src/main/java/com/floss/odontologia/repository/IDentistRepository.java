package com.floss.odontologia.repository;

import com.floss.odontologia.model.AppUser;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface IDentistRepository extends JpaRepository<AppUser,Long > {
}
