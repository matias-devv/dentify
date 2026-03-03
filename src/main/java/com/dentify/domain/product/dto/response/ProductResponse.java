package com.dentify.domain.product.dto.response;

import java.math.BigDecimal;

public record ProductResponse(Long id,
                              String name,
                              BigDecimal unit_price,
                              String description) {
}
