package com.floss.odontologia.service.interfaces;

import com.floss.odontologia.dto.response.AppointmentDTO;
import com.floss.odontologia.model.Appointment;
import com.floss.odontologia.model.AppUser;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

public interface IAppointmentService {

    //create
    public String createAppo(Appointment appointment);

    //read
    public AppointmentDTO getAppointmentById(Long id);

    public List<AppointmentDTO> getAllAppointments();

    public int getAppointmentNumberToday(Long id);

    public List<LocalTime> getHoursOfDentist(LocalDate choosenDate, Long id_dentist, String SelectedDay);

    public List<LocalTime> checkAppointments (LocalDate date, AppUser dentist, List<LocalTime> hours);

    //update
    public String editAppo(Appointment appointment);

    //delete
    public String deleteAppo(Long id);

    public AppointmentDTO setAttributesDto(Appointment appointment);

}
