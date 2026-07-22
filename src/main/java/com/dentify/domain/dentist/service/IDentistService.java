package com.dentify.domain.dentist.service;

import com.dentify.domain.clinic.model.Clinic;
import com.dentify.domain.dentist.model.Dentist;
import com.dentify.domain.secretary.model.Secretary;
import com.dentify.domain.userProfile.model.UserProfile;
import jakarta.validation.constraints.NotBlank;

import java.util.List;
import java.util.Set;

public interface IDentistService {

    Dentist findByIdWithProfileAndClinic(@NotBlank(message = "The dentist is mandatory") Long idDentist);

    void validateDentistEmail( String email);

    void createDentist( Set<Long> specialityIds, UserProfile newUserProfile, Clinic clinic);

    void persistDentist(Dentist dentist);

    Dentist findDentistByAuthUserUsername(String username);

    Dentist findByIdWithSecretaries(Long idDentist);

    Dentist resolveDentist( Long idDentist, String username);

    Long getDentistIdByUsername(String username);
}
