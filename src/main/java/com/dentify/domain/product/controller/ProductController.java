package com.dentify.domain.product.controller;

import com.dentify.domain.product.dto.response.ActiveProductResponse;
import com.dentify.domain.product.dto.ProductDTO;
import com.dentify.domain.product.service.IProductService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
public class ProductController {

    private final IProductService productService;

    @PreAuthorize("hasAnyRole('DENTIST','SECRETARY')")
    @PostMapping("/save")
    public ResponseEntity<String> saveProduct(@AuthenticationPrincipal String username,
                                              @Valid @RequestBody ProductDTO productDTO) {
        return ResponseEntity.status(HttpStatus.CREATED).body( productService.saveProduct( username, productDTO) );
    }

    @PreAuthorize("hasAnyRole('DENTIST','SECRETARY')")
    @PostMapping("/save/all")
    public ResponseEntity<String> saveAll( @AuthenticationPrincipal String username,
                                           @NotEmpty @Valid @RequestBody List<ProductDTO> products) {
        return ResponseEntity.status(HttpStatus.CREATED).body( productService.saveAll( username, products) );
    }

    @PreAuthorize("hasAnyRole('DENTIST','SECRETARY')")
    @GetMapping("/active")
    public List<ActiveProductResponse> getActiveProducts(@AuthenticationPrincipal String username){
        return productService.getActiveProducts(username);
    }
}
