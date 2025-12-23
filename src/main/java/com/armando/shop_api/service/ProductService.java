package com.armando.shop_api.service;

import com.armando.shop_api.dto.ProductRequest;
import com.armando.shop_api.dto.ProductResponse;

import java.util.List;

public interface ProductService {
    List<ProductResponse> list();
    ProductResponse get(Long id);
    ProductResponse create(ProductRequest req);
    ProductResponse update(Long id, ProductRequest req);
    void delete(Long id);
}
