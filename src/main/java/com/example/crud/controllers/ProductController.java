package com.example.crud.controllers;

import com.example.crud.domain.product.Product;
import com.example.crud.domain.product.ProductRepository;
import com.example.crud.domain.category.RequestCategory;
import com.example.crud.domain.product.RequestProduct;
import com.example.crud.service.AddressResponse;
import com.example.crud.service.AddressSearchService;
import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import java.util.Map;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/product")
public class ProductController {
    private final ProductRepository repository;
    private final AddressSearchService addressSearchService;

    @Autowired
    public ProductController(ProductRepository repository, AddressSearchService addressSearchService) {
        this.repository = repository;
        this.addressSearchService = addressSearchService;
    }

    @GetMapping
    public ResponseEntity getAllProducts(){
        var allProducts = repository.findAllByActiveTrue();
        return ResponseEntity.ok(allProducts);
    }

    @GetMapping("/cep")
    public ResponseEntity<String> verifyAvailability(@RequestParam String state, @RequestParam String city, @RequestParam String street) {
        AddressResponse[] addresses = addressSearchService.searchByAddress(state, city, street);

        if (addresses != null && addresses.length > 0) {
            String cep = addresses[0].cep();
            return ResponseEntity.ok(cep);
        } else {
            return ResponseEntity.notFound().build();
        }
    }
    @GetMapping("/address/{cep}")
    public ResponseEntity<AddressResponse> getAddressByCep(@PathVariable String cep) {
        AddressResponse address = addressSearchService.searchByCep(cep);
        return ResponseEntity.ok(address);
    }

    @GetMapping("/endpoint1") //products from only one category
    public ResponseEntity getAllProducts1(@RequestParam String categoryAsParam){
        var allProducts = repository.findAllByCategory(categoryAsParam);
        return ResponseEntity.ok(allProducts);
    }

    @GetMapping("/endpoint2/{id}") //only one product
    public ResponseEntity getProduct(@PathVariable String id){
        Optional<Product> optionalProduct = repository.findById(id);
        return ResponseEntity.ok(optionalProduct);
    }

    @GetMapping("/endpoint3/top5byprice") // top 5 product by price
    public ResponseEntity getAllProducts3(){
        var allProducts = repository.findAllByActiveTrue();

        List<Product> topFive = allProducts
                .stream()
                .sorted(Comparator.comparingInt(Product::getPrice).reversed())
                .limit(5)
                .collect(Collectors.toList());

        return ResponseEntity.ok(topFive);
    }

    @GetMapping("/category/{categoryAsPath}") //all REST Components
    public ResponseEntity getProductsByCategory(
            @RequestHeader String categoryAsHeader,
            @PathVariable String categoryAsPath,
            @RequestBody @Valid RequestCategory categoryAsBody,
            @RequestParam String categoryAsParam
    ){
        var allProducts = repository.findAllByActiveTrue();
        List<Product> filteredProducts = new ArrayList<>();

        for (int i = 0; i < allProducts.size(); i++) {
            Product product = allProducts.get(i);
            if (categoryAsParam.equals(product.getCategory())) {
                filteredProducts.add(product);
            }
        }
        return ResponseEntity.ok(filteredProducts);
    }

    @PostMapping
    public ResponseEntity registerProduct(@RequestBody @Valid RequestProduct data){
        Product newProduct = new Product(data);
        repository.save(newProduct);
        return ResponseEntity.ok().build();
    }

    @PutMapping
    @Transactional
    public ResponseEntity updateProduct(@RequestBody @Valid RequestProduct data){
        Optional<Product> optionalProduct = repository.findById(data.id());
        if (optionalProduct.isPresent()) {
            Product product = optionalProduct.get();
            product.setName(data.name());
            product.setPrice(data.price());
            return ResponseEntity.ok(product);
        } else {
            throw new EntityNotFoundException();
        }
    }

    @DeleteMapping("/{id}")
    @Transactional
    public ResponseEntity deleteProduct(@PathVariable String id){
        Optional<Product> optionalProduct = repository.findById(id);
        if (optionalProduct.isPresent()) {
            Product product = optionalProduct.get();
            product.setActive(false);
            return ResponseEntity.noContent().build();
        } else {
            throw new EntityNotFoundException();
        }
    }
    @GetMapping("/{productId}/check-availability/{cep}")
    public ResponseEntity<Map<String, Boolean>> checkProductAvailability(
            @PathVariable String productId,
            @PathVariable String cep) {


        Optional<Product> optionalProduct = repository.findById(productId);

        if (optionalProduct.isEmpty()) {
            throw new EntityNotFoundException("Produto não encontrado");
        }
        Product product = optionalProduct.get();

        Boolean isAvailable = addressSearchService.checkDistributionCenterAvailability(cep, product);

        Map<String, Boolean> response = Map.of("available", isAvailable);
        return ResponseEntity.ok(response);
    }

}
