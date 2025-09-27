package br.edu.atitus.product_service.controllers;

import br.edu.atitus.product_service.entities.ProductEntity;
import br.edu.atitus.product_service.repositories.ProductRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/products")
public class ProductController {

    private final ProductRepository repository;

    @Value("${server.port}")
    private int serverPort;

    public ProductController(ProductRepository repository) {
        this.repository = repository;
    }

    @GetMapping
    public ResponseEntity<List<ProductEntity>> listAll() {
        return ResponseEntity.ok(repository.findAll());
    }

    @GetMapping("/search/{name}")
    public ResponseEntity<List<ProductEntity>> searchByName(@PathVariable String name) {
        return ResponseEntity.ok(repository.findByNameContainingIgnoreCase(name));
    }

    @PostMapping
    public ResponseEntity<ProductEntity> save(@RequestBody ProductEntity product) {
        ProductEntity saved = repository.save(product);
        return ResponseEntity.ok(saved);
    }
}
