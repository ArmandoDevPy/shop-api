package com.armando.shop_api.service;

import com.armando.shop_api.dto.ProductRequest;
import com.armando.shop_api.dto.ProductResponse;

import java.util.List;

public interface ProductService {
    ProductResponse create(ProductRequest req);
    List<ProductResponse> list();
    ProductResponse get(Long id);
    ProductResponse update(Long id, ProductRequest req);
    void delete(Long id);
}
