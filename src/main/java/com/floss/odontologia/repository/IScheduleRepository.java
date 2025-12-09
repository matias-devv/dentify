package com.floss.odontologia.repository;

import com.floss.odontologia.model.Schedule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface IScheduleRepository extends JpaRepository<Schedule, Long> {
}
