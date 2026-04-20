package com.dentify.domain.clinic.service;

import com.dentify.domain.clinic.dto.ClinicData;
import com.dentify.domain.clinic.model.Clinic;
import com.dentify.domain.clinic.repository.IClinicRepository;
import com.dentify.exception.clinic.ClinicCuitAlreadyExistsException;
import com.dentify.exception.clinic.ClinicEmailAlreadyExistsException;
import com.dentify.exception.clinic.ClinicNotFoundException;
import com.dentify.domain.userProfile.model.UserProfile;
import com.dentify.mapper.ClinicMapper;
import com.dentify.security.model.AuthUser;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ClinicService implements IClinicService {

    //repository
    private final IClinicRepository clinicRepository;

    //mapper
    private final ClinicMapper clinicMapper;

    @Override
    public Clinic verifyIfClinicExists(AuthUser authInviter, UserProfile inviter, ClinicData newClinic) {

        boolean ok = authInviter.hasRole("DENTIST");

        if (ok) {

            Clinic clinic = inviter.getClinic();

            if ( clinic == null ) throw new ClinicNotFoundException("The inviting dentist has no registered clinic");

            return clinic;

        } else {
            return this.createClinic( newClinic );
        }
    }

    @Override
    public Clinic findClinicById(Long idClinic) {
        return clinicRepository.findById(idClinic).orElseThrow( ()-> new ClinicNotFoundException("The clinic with this id was not found"));
    }

    @Override
    @Transactional
    public Clinic createClinic( ClinicData data) {

        this.validateCreation(data);

        Clinic clinic = clinicMapper.buildClinic(data);

        return clinicRepository.save(clinic);
    }

    private void validateCreation(ClinicData data) {

        if ( clinicRepository.existsByCuit( data.clinicCuit() ) ) {
            throw new ClinicCuitAlreadyExistsException("The cuit number assigned to this new clinic already exists.");
        }
        if ( clinicRepository.existsByEmail( data.clinicEmail() ) ) {
            throw new ClinicEmailAlreadyExistsException("The email assigned to this new clinic already exists.");
        }
    }
}
