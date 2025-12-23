package com.armando.shop_api.service.impl;

import com.armando.shop_api.dto.ProductRequest;
import com.armando.shop_api.dto.ProductResponse;
import com.armando.shop_api.entity.Product;
import com.armando.shop_api.exception.NotFoundException;
import com.armando.shop_api.repository.ProductRepository;
import com.armando.shop_api.service.ProductService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ProductServiceImpl implements ProductService {

    private final ProductRepository repo;

    public ProductServiceImpl(ProductRepository repo) {
        this.repo = repo;
    }

    @Override
    public List<ProductResponse> list() {
        return repo.findAll().stream()
                .map(this::toResponse)
                .toList();
    }

    @Override
    public ProductResponse get(Long id) {
        Product p = repo.findById(id)
                .orElseThrow(() -> new NotFoundException("Product not found"));
        return toResponse(p);
    }

    @Override
    public ProductResponse create(ProductRequest req) {
        Product p = new Product();
        p.setName(req.getName());
        p.setPrice(req.getPrice());
        p.setStock(req.getStock());
        return toResponse(repo.save(p));
    }

    @Override
    public ProductResponse update(Long id, ProductRequest req) {
        Product p = repo.findById(id)
                .orElseThrow(() -> new NotFoundException("Product not found"));

        p.setName(req.getName());
        p.setPrice(req.getPrice());
        p.setStock(req.getStock());

        return toResponse(repo.save(p));
    }

    @Override
    public void delete(Long id) {
        if (!repo.existsById(id)) {
            throw new NotFoundException("Product not found");
        }
        repo.deleteById(id);
    }

    private ProductResponse toResponse(Product p) {
        return new ProductResponse(p.getId(), p.getName(), p.getPrice(), p.getStock());
    }
}
