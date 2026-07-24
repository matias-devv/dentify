package com.dentify.domain.diagnosistypecatalog.controller;

import com.dentify.domain.diagnosistypecatalog.service.IDiagnosisTypeCatalogService;
import com.mercadopago.net.HttpStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/diagnosis-type-catalog")
@RequiredArgsConstructor
public class DiagnosisTypeCatalogController {

    private final IDiagnosisTypeCatalogService diagnosisTypeCatalogService;

    @PreAuthorize("hasAnyRole('DENTIST','SECRETARY')")
    @GetMapping("")
    public ResponseEntity<List> listAccessibleDiagnosisTypes(@AuthenticationPrincipal String username) {

        return ResponseEntity.status(HttpStatus.OK).body( diagnosisTypeCatalogService.listAccessibleDiagnosisTypes(username));
    }

}
