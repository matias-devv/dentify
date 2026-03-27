package com.dentify.domain.product.service;

import com.dentify.domain.agenda.model.Agenda;
import com.dentify.domain.clinic.model.Clinic;
import com.dentify.domain.pay.service.IPayService;
import com.dentify.domain.userProfile.service.IUserProfileService;
import com.dentify.exception.product.InactiveProductException;
import com.dentify.exception.product.ProductAlreadyExistsException;
import com.dentify.exception.product.ProductNotFoundException;
import com.dentify.domain.product.dto.response.ActiveProductResponse;
import com.dentify.domain.product.dto.ProductDTO;
import com.dentify.domain.product.model.Product;
import com.dentify.domain.speciality.model.Speciality;
import com.dentify.domain.product.repository.IProductRepository;
import com.dentify.domain.speciality.service.ISpecialityService;
import com.dentify.mapper.ProductMapper;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProductService implements IProductService {

    //repository
    private final IProductRepository productRepository;
    //services
    private final ISpecialityService specialityService;
    private final IUserProfileService userProfileService;
    private final IPayService payService;

    //mapper
    private final ProductMapper productMapper;


    @Override
    @Transactional
    public String saveProduct(  String username, ProductDTO dto) {

        payService.validateAmountQuantity( dto.unitPrice() );

        Clinic clinic = userProfileService.findClinicByAuthUserUsername(username);

        this.verifyIfProductAlreadyExists( dto.nameProduct(), clinic.getId_clinic() );

        Product product = productMapper.buildProduct(dto, clinic);

        Speciality speciality = specialityService.getSpecialityEntityById(dto.idSpeciality());

        product.setSpeciality(speciality);

        this.persistSingleProduct(product);

        return "the product was saved successfully";
    }

    private void persistSingleProduct(Product product) {
        try {
            productRepository.save(product);
        }catch(DataIntegrityViolationException e) {
            throw new ProductAlreadyExistsException("Product name already exist");
        }
    }

    @Override
    public Product findProductById(Long id) {
        return productRepository.findProductById( id ).orElseThrow( () -> new ProductNotFoundException("Product not found"));
    }

    @Override
    @Transactional
    public String saveAll( String username, List<ProductDTO> products) {

        this.validateBulkLimit(products.size());

        Clinic clinic = userProfileService.findClinicByAuthUserUsername( username);

        List<Product> newProducts = new ArrayList<>();

        Map<Long, Speciality> specialityMap = this.getSpecialityMap(products);

        Map<String, Long> existingNames = this.getProductMap(products, clinic);

        products.forEach(dto -> {

            this.verifyProductName(existingNames.containsKey(dto.nameProduct()));

            payService.validateAmountQuantity(dto.unitPrice());

            Product product = productMapper.buildProduct(dto, clinic);

            Speciality speciality = specialityMap.get(dto.idSpeciality());

            specialityService.verifyIfSpecialityExists(speciality);

            product.setSpeciality(speciality);

            newProducts.add(product);
        });

        this.persistAllProducts(newProducts);

        return "All the products was saved successfully";
    }

    private Map<Long, Speciality> getSpecialityMap(List<ProductDTO> products) {
        Set<Long> specialityIds = this.getSpecialityIdsByProductList(products);

        return specialityService.findAllByIds(specialityIds);
    }

    private Set<Long> getSpecialityIdsByProductList(List<ProductDTO> products) {
        return products.stream()
                .map(ProductDTO::idSpeciality)
                .collect(Collectors.toSet());
    }

    private Map<String, Long> getProductMap(List<ProductDTO> products, Clinic clinic) {
        Set<String> productNames = this.getProductsNameByList(products);

        return this.findAllByNames(productNames, clinic);
    }

    private Set<String> getProductsNameByList(List<ProductDTO> products) {
        return products.stream()
                .map(ProductDTO::nameProduct)
                .collect(Collectors.toSet());
    }

    private void verifyProductName(boolean exists) {
        if (exists){
            throw new ProductAlreadyExistsException("The name assigned to this product already exists");
        }
    }

    private Map<String, Long> findAllByNames(Set<String> productNames, Clinic clinic) {

        List<Product> products = productRepository.findProductNameAndIdByNamesAndClinic(productNames, clinic);
        Map<String, Long> productMap = new HashMap<>();

        products.forEach(p -> { productMap.put(p.getNameProduct(), p.getId_product() ); });

        return productMap;
    }

    private void validateBulkLimit(int size) {
        if (size > 50) {
            throw new IllegalArgumentException("Bulk limit is 50 products per request");
        }
    }

    private void verifyIfProductAlreadyExists(String name, Long clinicId) {
        if ( productRepository.existsByNameAndClinicId(name, clinicId) ){
            throw new ProductAlreadyExistsException("The name assigned to this product already exists");
        }
    }

    private void persistAllProducts(List<Product> newProducts) {
        try {
            productRepository.saveAll(newProducts);
        } catch (DataIntegrityViolationException ex) {
            throw new ProductAlreadyExistsException("One or more product names already exist");
        }
    }

    @Override
    public void validateIfProductIsActive(boolean active) {
        if (!active) {
            throw new InactiveProductException("This product is not active at the moment");
        }
    }

    @Override
    public List<ActiveProductResponse> getActiveProducts(String username) {

        Clinic clinic = userProfileService.findClinicByAuthUserUsername( username);

        List<Product> products = productRepository.findAllActiveWithSpeciality(clinic);

        return productMapper.buildActiveProductResponseList(products);
    }

    @Override
    public void setProductToAgenda(Long idProduct, Agenda agenda) {

        if (idProduct == null) return;

        Product product = this.findProductById(idProduct);

        agenda.setProduct(product);
    }
}
