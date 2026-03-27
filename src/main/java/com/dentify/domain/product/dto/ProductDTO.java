package com.dentify.domain.product.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record ProductDTO(

        @NotBlank String nameProduct,

        @NotNull @DecimalMin(value = "0.1", message = "The price must be bigger than zero") BigDecimal unitPrice,

        @NotNull Long idSpeciality,

        String description,

        @NotNull Boolean active
) {}
