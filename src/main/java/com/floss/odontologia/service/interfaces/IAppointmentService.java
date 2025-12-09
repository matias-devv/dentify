package com.floss.odontologia.service.interfaces;

import com.floss.odontologia.model.Appointment;
import com.floss.odontologia.model.Dentist;

import java.util.List;

public interface IAppointmentService {

    //create
    public String createAppo(Appointment appointment);
    //read
    public Appointment getAppointmentById(Long id);
    public List<Appointment> getAllAppointments();
    public int getAppointmentNumberToday(Dentist dentist);

    //update
    public String editAppo(Appointment appointment);
    //delete
    public String deleteAppo(Long id);


}
