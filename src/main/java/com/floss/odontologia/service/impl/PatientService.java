package com.floss.odontologia.service.impl;

import com.floss.odontologia.model.Patient;
import com.floss.odontologia.repository.IPatientRepository;
import com.floss.odontologia.service.interfaces.IPatientService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class PatientService implements IPatientService {

    @Autowired
    private IPatientRepository iPatientRepository;

    @Override
    public String createPatient(Patient patient) {
        iPatientRepository.save(patient);
        return "The patient was saved successfully";
    }

    @Override
    public Patient getPatient(String dni) {

        List<Patient> list = this.getPatients();
        Patient patient = new Patient();

        for (Patient pa : list) {
            //If the dni is the same -> return patient
            if ( pa.getDni().equals(dni)) {
                patient = iPatientRepository.findById(pa.getId_patient()).orElse(null);
                return patient;
            }
        }
        return patient;
    }

    @Override
    public List<Patient> getPatients() {
        return iPatientRepository.findAll();
    }

    @Override
    public int getTotalOfPatients() {
        return iPatientRepository.findAll().size();
    }

    @Override
    public List<Patient> getPatientsWithInsurance() {
        List<Patient> allPatients = this.getPatients();
        List<Patient> listWithInsurance = new ArrayList<>();

        for (Patient pa : allPatients) {
            if(pa.getInsurance() == true){
                listWithInsurance.add(pa);
            }
        }
        return listWithInsurance;
    }

    @Override
    public List<Patient> getPatientsWithoutInsurance() {
        List<Patient> allPatients = this.getPatients();
        List<Patient> listWithoutInsurance = new ArrayList<>();

        for (Patient pa : allPatients) {
            if(pa.getInsurance() == false){
                listWithoutInsurance.add(pa);
            }
        }
        return listWithoutInsurance;
    }

    @Override
    public String editPatient(Patient patient) {
        iPatientRepository.save(patient);
        return "The patient was edited successfully";
    }

    @Override
    public String deletePatient(Long id) {
        try{
            iPatientRepository.deleteById(id);
            return "The patient was deleted successfully";
        }catch (Exception e) {
            return "The patient does not exist in the database";
        }
    }
}
