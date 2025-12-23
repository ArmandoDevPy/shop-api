package com.armando.shop_api.controller;

import com.armando.shop_api.dto.OrderCreateRequest;
import com.armando.shop_api.dto.OrderResponse;
import com.armando.shop_api.service.OrderService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/orders")
public class OrderController {

    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    // 🔒 Crear orden (requiere JWT)
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public OrderResponse create(@Valid @RequestBody OrderCreateRequest req, Authentication auth) {
        return orderService.create(req, auth.getName());
    }

    // 🔒 Actualizar mi orden
    @PutMapping("/{id}")
    public OrderResponse update(
            @PathVariable Long id,
            @Valid @RequestBody OrderCreateRequest req,
            Authentication auth) {
        return orderService.update(id, req, auth.getName());
    }

    // 🔒 Eliminar mi orden
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id, Authentication auth) {
        orderService.delete(id, auth.getName());
    }

    // 🔒 Listar mis órdenes
    @GetMapping
    public List<OrderResponse> listMine(Authentication auth) {
        return orderService.listMine(auth.getName());
    }

    // 🔒 Ver mi orden por id
    @GetMapping("/{id}")
    public OrderResponse getMine(@PathVariable Long id, Authentication auth) {
        return orderService.getMine(id, auth.getName());
    }

}
