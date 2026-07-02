package com.dentify.domain.toothrecord.repository;

import com.dentify.domain.toothrecord.model.ToothRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface IToothRecordRepository extends JpaRepository<ToothRecord, Long> {
}
