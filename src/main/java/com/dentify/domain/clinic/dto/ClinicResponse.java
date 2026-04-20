package com.dentify.domain.clinic.dto;

public record ClinicResponse(Long id,
                             String name,
                             String diretion,
                             String cuit,
                             String phone_number,
                             String email,
                             String createdAt) {
}
