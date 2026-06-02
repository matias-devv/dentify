package com.dentify.domain.allergycatalog.service;

import com.dentify.domain.allergycatalog.model.AllergyCatalog;

import java.util.List;

public interface IAllergyCatalogService {

    List<AllergyCatalog> findAllergiesWithThisIds(List<Long> ids);

    int seedAllergies();
}
