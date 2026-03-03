package com.dentify.domain.product.dto.response;

import java.math.BigDecimal;

public record ActiveProductResponse(Long id_product,
                                    String name_product,
                                    BigDecimal unit_price,
                                    String description,
                                    Long id_speciality,
                                    String name_speciality) {
}
