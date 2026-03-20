package com.dentify.mapper;

import com.dentify.domain.speciality.model.Speciality;
import org.springframework.stereotype.Component;

@Component
public class SpecialityMapper {

    public Speciality buildSpeciality(String name){
        return Speciality.builder()
                         .name(name)
                         .build();
    }
}
