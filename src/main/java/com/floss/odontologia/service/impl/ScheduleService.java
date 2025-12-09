package com.floss.odontologia.service.impl;

import com.floss.odontologia.model.Schedule;
import com.floss.odontologia.repository.IScheduleRepository;
import com.floss.odontologia.service.interfaces.IScheduleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ScheduleService implements IScheduleService {

    @Autowired
    private IScheduleRepository iScheduleRepository;

    @Override
    public String createSchedule(Schedule schedule) {
        iScheduleRepository.save(schedule);
        return "The schedule was saved successfully";
    }

    @Override
    public Schedule getScheduleById(Long id) {
        return iScheduleRepository.findById(id).orElse(null);
    }

    @Override
    public List<Schedule> getAllSchedules() {
        return iScheduleRepository.findAll();
    }

    @Override
    public void editSchedule(Schedule schedule) {
        iScheduleRepository.save(schedule);
    }
}
