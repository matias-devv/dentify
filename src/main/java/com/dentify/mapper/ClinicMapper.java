package com.dentify.mapper;

import com.dentify.domain.clinic.dto.ClinicData;
import com.dentify.domain.clinic.dto.ClinicResponse;
import com.dentify.domain.clinic.model.Clinic;
import org.springframework.stereotype.Component;

import java.time.format.DateTimeFormatter;
import java.util.UUID;

@Component
public class ClinicMapper {

    public Clinic buildClinic(ClinicData request) {
        return Clinic.builder()
                .name(request.clinicName())
                .direction(request.clinicDirection())
                .cuit(request.clinicCuit())
                .phone_number(request.clinicPhone())
                .email(request.clinicEmail())
                .tenantId( UUID.randomUUID().toString() )
                .active(true)
                .build();
    }

    public ClinicResponse toResponse(Clinic clinic) {
        return new ClinicResponse(
                clinic.getId(),
                clinic.getName(),
                clinic.getDirection(),
                clinic.getCuit(),
                clinic.getPhone_number(),
                clinic.getEmail(),
                clinic.getCreatedAt().format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))
        );
    }
}
