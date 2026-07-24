package com.dentify.mapper;

import com.dentify.domain.allergycatalog.dto.response.AllergyCatalogResponse;
import com.dentify.domain.allergycatalog.model.AllergyCatalog;
import org.springframework.stereotype.Component;

@Component
public class AllergyCatalogMapper {

    public AllergyCatalogResponse toResponse(AllergyCatalog allergy) {
        return new AllergyCatalogResponse(allergy.getId(), allergy.getName(), allergy.getActive());
    }
}
