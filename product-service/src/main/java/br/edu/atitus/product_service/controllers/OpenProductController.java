package br.edu.atitus.product_service.controllers;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import br.edu.atitus.product_service.entities.ProductEntity;
import br.edu.atitus.product_service.services.ProductService;

@RestController
@RequestMapping("products")
public class OpenProductController {

    private final ProductService service;

    public OpenProductController(ProductService service) {
        this.service = service;
    }

    @GetMapping("/{idProduct}/{targetCurrency}")
    public ResponseEntity<ProductEntity> getProduct(
            @PathVariable Long idProduct,
            @PathVariable String targetCurrency
    ) throws Exception {
        ProductEntity product = service.getProductById(idProduct, targetCurrency);
        return ResponseEntity.ok(product);
    }
}