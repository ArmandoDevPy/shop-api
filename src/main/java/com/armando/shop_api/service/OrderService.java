package com.armando.shop_api.service;

import com.armando.shop_api.dto.OrderCreateRequest;
import com.armando.shop_api.dto.OrderResponse;

import java.util.List;

public interface OrderService {
    OrderResponse create(OrderCreateRequest req, String userEmail);
    List<OrderResponse> listMine(String userEmail);
    OrderResponse getMine(Long id, String userEmail);

    OrderResponse update(Long id, OrderCreateRequest req, String userEmail);
    void delete(Long id, String userEmail);
}
