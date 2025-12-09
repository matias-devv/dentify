package com.floss.odontologia.service.impl;

import com.floss.odontologia.model.Appointment;
import com.floss.odontologia.model.Dentist;
import com.floss.odontologia.model.Patient;
import com.floss.odontologia.repository.IAppointmentRepository;
import com.floss.odontologia.repository.IDentistRepository;
import com.floss.odontologia.repository.IPatientRepository;
import com.floss.odontologia.service.interfaces.IDentistService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class DentistService implements IDentistService {

    @Autowired
    private IDentistRepository iDentistRepository;

    @Autowired
    private IAppointmentRepository iAppointmentRepository;

    @Autowired
    private IPatientRepository iPatientRepository;

    @Override
    public String createDentist(Dentist dentist) {
        iDentistRepository.save(dentist);
        return "The dentist was created successfully";
    }

    @Override
    public Dentist getDentistById(Long id) {
        return iDentistRepository.findById(id).orElse(null);
    }

    @Override
    public List<Dentist> getAllDentists() {
        return iDentistRepository.findAll();
    }

    @Override
    public List<Appointment> getAppointmentsByDentist(Dentist dentist) {

        List <Appointment> listAppo = iAppointmentRepository.findAll();
        List <Appointment> cleanList = new ArrayList<>();

        for ( Appointment appo : listAppo){
            //If the dentist are the same as the one asigned to the appointment -> add it to the clean list
            if ( appo.getDentist().getId_dentist() == dentist.getId_dentist()){
                cleanList.add(appo);
            }
        }
        return cleanList;
    }

    @Override
    public List<Patient> getPatientsByDentist(Dentist dentist) {

        List <Patient> patientList = iPatientRepository.findAll();
        List <Patient> cleanList = new ArrayList<>();

        for (Patient patient : patientList){

            //I get the appointments of the current patient
            List <Appointment> appoList = patient.getAppointments();

            for (Appointment appo : appoList){
                //If the dentist asigned to the appointment is the same as the one that I received -> add it patient to the cleanList
                if( appo.getDentist().getId_dentist() == dentist.getId_dentist()){
                    cleanList.add(patient);

                }
            }
        }
        return cleanList;
    }

    @Override
    public String editDentist(Dentist dentist) {
        iDentistRepository.save(dentist);
        return "The dentist was edited succesfully";
    }
}
