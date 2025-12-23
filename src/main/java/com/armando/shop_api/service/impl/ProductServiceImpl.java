package com.armando.shop_api.service.impl;

import com.armando.shop_api.dto.ProductRequest;
import com.armando.shop_api.dto.ProductResponse;
import com.armando.shop_api.entity.Product;
import com.armando.shop_api.repository.ProductRepository;
import com.armando.shop_api.service.ProductService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ProductServiceImpl implements ProductService {

    private final ProductRepository repo;

    public ProductServiceImpl(ProductRepository repo) {
        this.repo = repo;
    }

    private static ProductResponse toRes(Product p) {
        return new ProductResponse(p.getId(), p.getName(), p.getPrice(), p.getStock());
    }

    @Override
    public List<ProductResponse> list() {
        return repo.findAll().stream().map(ProductServiceImpl::toRes).toList();
    }

    @Override
    public ProductResponse get(Long id) {
        var p = repo.findById(id).orElseThrow(() -> new RuntimeException("Product not found"));
        return toRes(p);
    }

    @Override
    @Transactional
    public ProductResponse create(ProductRequest req) {
        var p = new Product();
        p.setName(req.getName());
        p.setPrice(req.getPrice());
        p.setStock(req.getStock());
        return toRes(repo.save(p));
    }

    @Override
    @Transactional
    public ProductResponse update(Long id, ProductRequest req) {
        var p = repo.findById(id).orElseThrow(() -> new RuntimeException("Product not found"));
        p.setName(req.getName());
        p.setPrice(req.getPrice());
        p.setStock(req.getStock());
        return toRes(repo.save(p));
    }

    @Override
    @Transactional
    public void delete(Long id) {
        if (!repo.existsById(id)) throw new RuntimeException("Product not found");
        repo.deleteById(id);
    }
}
