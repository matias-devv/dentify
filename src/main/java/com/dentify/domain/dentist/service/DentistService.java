package com.dentify.domain.dentist.service;

import com.dentify.domain.clinic.model.Clinic;
import com.dentify.domain.dentist.model.Dentist;
import com.dentify.domain.dentist.repository.IDentistRepository;
import com.dentify.domain.speciality.model.Speciality;
import com.dentify.domain.speciality.service.ISpecialityService;
import com.dentify.exception.agenda.MissingParameterException;
import com.dentify.exception.dentist.DentistIdMismatchException;
import com.dentify.exception.dentist.DentistNotFoundException;
import com.dentify.domain.userProfile.model.UserProfile;
import com.dentify.mapper.DentistMapper;
import com.dentify.security.model.AuthUser;
import com.dentify.security.multitenancy.TenantContext;
import com.dentify.security.service.IAuthUserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;

@Service
@Slf4j
@RequiredArgsConstructor
public class DentistService implements IDentistService {

    //repository
    private final IDentistRepository dentistRepository;

    //services
    private final ISpecialityService specialityService;
    private final IAuthUserService authUserService;

    //mapper
    private final DentistMapper dentistMapper;

    /**
    * Querys
    */
    @Override
    public Dentist findByIdWithProfileAndClinic(Long id) {

        Dentist dentist = dentistRepository.findByIdWithProfileAndClinic( id )
                                           .orElseThrow( () -> new DentistNotFoundException("The dentist with this id was not found"));

        if ( !dentist.getClinic().getTenantId().equals(TenantContext.get() ) ) {
            throw new DentistNotFoundException("The dentist with this id was not found");
        }
        return dentist;
    }

    @Override
    public Dentist findByIdWithSecretaries( Long idDentist) {

        return dentistRepository.findByIdWithSecretaries( idDentist)
                                .orElseThrow( () -> new DentistNotFoundException("The dentist with this id was not found"));
    }

    @Override
    public Dentist findDentistByAuthUserUsername(String username) {

        return dentistRepository.findDentistByAuthUserUsername( username)
                                .orElseThrow( ()-> new DentistNotFoundException("The dentist with this username was not found"));
    }

    /**
     * Logic
     */
    @Override
    public void validateDentistEmail(String email) {

        if ( email == null || email.isEmpty()){
            log.warn("The email of the patient cannot be empty");
        }
    }

    @Override
    public void createDentist( Set<Long> specialityIds, UserProfile newUserProfile, Clinic clinic) {

        Set<Speciality> specialityList = specialityService.findAllByIdIn( specialityIds);

        Dentist dentist = dentistMapper.buildDentist ( newUserProfile, specialityList, clinic);

        this.persistDentist(dentist);
    }

    @Override
    public void persistDentist(Dentist dentist) {
        dentistRepository.save(dentist);
    }


    @Override
    public Dentist resolveDentist( Long idDentist, String username) {

        AuthUser caller = authUserService.findAuthUserByUsername( username);

        boolean isDentist = caller.hasRole("DENTIST" );

        if( isDentist ) {
            //get dentist
            Dentist dentistFound = this.findDentistByAuthUserUsername(username);

            if ( idDentist != null) {
                if ( !dentistFound.getId().equals(idDentist) ) throw new DentistIdMismatchException("The actual dentist cannot assign agendas to other dentist");
            }

            return dentistFound;
        }
        else{

            if (idDentist == null) {
                throw new MissingParameterException("idDentist is required for Admin and Secretary");
            }

            Dentist dentist = this.findByIdWithProfileAndClinic(idDentist);

            if ( !dentist.getClinic().getTenantId().equals(TenantContext.get() ) ) {
                throw new DentistNotFoundException("The dentist with this id was not found");
            }
            return dentist;
        }
    }

    @Override
    public Long getDentistIdByUsername(String username) {
        return dentistRepository.findDentistIdByAuthUserUsername(username)
                                .orElseThrow(() -> new DentistNotFoundException("Dentist not found for username: " + username));
    }

}
