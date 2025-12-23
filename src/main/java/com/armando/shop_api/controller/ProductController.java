package com.armando.shop_api.controller;

import com.armando.shop_api.dto.*;
import com.armando.shop_api.entity.Product;
import com.armando.shop_api.repository.ProductRepository;
import jakarta.validation.Valid;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/products")
public class ProductController {

    private final ProductRepository productRepository;

    public ProductController(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    // ✅ GET público
    @GetMapping
    public List<ProductResponse> list() {
        return productRepository.findAll().stream()
                .map(p -> new ProductResponse(p.getId(), p.getName(), p.getPrice(), p.getStock()))
                .toList();
    }

    // ✅ GET público (si lo tienes)
    @GetMapping("/{id}")
    public ProductResponse get(@PathVariable Long id) {
        var p = productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Product not found"));
        return new ProductResponse(p.getId(), p.getName(), p.getPrice(), p.getStock());
    }

    // 🔒 POST protegido
    @PostMapping
    public ResponseEntity<ProductResponse> create(@Valid @RequestBody ProductCreateRequest req) {
        Product p = new Product();
        p.setName(req.getName());
        p.setPrice(req.getPrice());
        p.setStock(req.getStock());

        p = productRepository.save(p);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new ProductResponse(p.getId(), p.getName(), p.getPrice(), p.getStock()));
    }

    // 🔒 PUT protegido
    @PutMapping("/{id}")
    public ProductResponse update(@PathVariable Long id, @Valid @RequestBody ProductUpdateRequest req) {
        var p = productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Product not found"));

        p.setName(req.getName());
        p.setPrice(req.getPrice());
        p.setStock(req.getStock());

        p = productRepository.save(p);

        return new ProductResponse(p.getId(), p.getName(), p.getPrice(), p.getStock());
    }

    // 🔒 DELETE protegido
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        if (!productRepository.existsById(id)) {
            throw new RuntimeException("Product not found");
        }
        productRepository.deleteById(id);
    }
}
