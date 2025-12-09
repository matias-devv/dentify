package com.floss.odontologia.service.impl;

import com.floss.odontologia.model.Appointment;
import com.floss.odontologia.model.Dentist;
import com.floss.odontologia.repository.IAppointmentRepository;
import com.floss.odontologia.service.interfaces.IAppointmentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
public class AppointmentService implements IAppointmentService {

    @Autowired
    private IAppointmentRepository iAppointmentRepository;

    @Override
    public String createAppo(Appointment appointment) {
        iAppointmentRepository.save(appointment);
        return "The appointment was saved correctly";
    }

    @Override
    public Appointment getAppointmentById(Long id) {
        return iAppointmentRepository.findById(id).orElse(null);
    }

    @Override
    public List<Appointment> getAllAppointments() {
        return iAppointmentRepository.findAll();
    }

    @Override
    public int getAppointmentNumberToday(Dentist dentist) {
        int total = 0;
        LocalDate today = LocalDate.now();

        //I'm bringing the dentist appointments
        List <Appointment> listAppo = dentist.getAppointmentList();

        for (Appointment appo : listAppo){

            LocalDate dateAppo = appo.getDate();
            if (dateAppo.equals(today)){
                total++;
            }
        }
        return total;
    }

    @Override
    public String editAppo(Appointment appointment) {
        iAppointmentRepository.save(appointment);
        return "The appointment was edited correctly";
    }

    @Override
    public String deleteAppo(Long id) {
        try {
            iAppointmentRepository.deleteById(id);
            return "The appointment was deleted correctly";
        }catch(Exception e) {
            return "The appointment does not exist in the database";
        }
    }
}
