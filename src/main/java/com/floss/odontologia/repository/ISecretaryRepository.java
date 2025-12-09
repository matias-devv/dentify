package com.floss.odontologia.repository;

import com.floss.odontologia.model.Secretary;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ISecretaryRepository extends JpaRepository<Secretary,Long> {
}
