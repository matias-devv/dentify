package com.dentify.domain.allergycatalog.controller;

import com.dentify.domain.allergycatalog.service.IAllergyCatalogService;
import com.mercadopago.net.HttpStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/allergies")
@RequiredArgsConstructor
public class AllergyCatalogController {

    private final IAllergyCatalogService allergyCatalogService;

    @PreAuthorize("hasAnyRole('DENTIST','SECRETARY')")
    @GetMapping("")
    public ResponseEntity<List> findAllActiveAllergies() {

        return ResponseEntity.status(HttpStatus.OK).body(allergyCatalogService.findAllActiveAllergies());
    }

}