package com.dentify.mapper;

import com.dentify.domain.clinic.model.Clinic;
import com.dentify.domain.product.dto.ProductDTO;
import com.dentify.domain.product.dto.response.ProductResponse;
import com.dentify.domain.appointment.model.Appointment;
import com.dentify.domain.product.dto.response.ActiveProductResponse;
import com.dentify.domain.product.model.Product;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class ProductMapper {

    public Product buildProduct(ProductDTO dto, Clinic clinic) {
        return Product.builder()
                .nameProduct(dto.nameProduct())
                .unitPrice(dto.unitPrice())
                .description(dto.description())
                .active(dto.active())
                .clinic(clinic)
                .build();
    }

    public ProductResponse buildProductResponse(Appointment appointment) {

        var treatment = appointment.getTreatment();
        if (treatment == null) return null;

        var product = treatment.getProduct();
        if (product == null) return null;

        return new ProductResponse(
                product.getId_product(),
                product.getNameProduct(),
                product.getUnitPrice(),
                product.getDescription()
        );
    }

    public List<ActiveProductResponse> buildActiveProductResponseList(List<Product> products) {

        return products.stream()
                .map(p -> new ActiveProductResponse(
                        p.getId_product(),
                        p.getNameProduct(),
                        p.getUnitPrice(),
                        p.getDescription(),
                        p.getSpeciality().getId(),
                        p.getSpeciality().getName()
                ))
                .collect(Collectors.toList());
    }
}
