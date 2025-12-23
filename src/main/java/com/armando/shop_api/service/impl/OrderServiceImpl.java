package com.armando.shop_api.service.impl;

import com.armando.shop_api.dto.*;
import com.armando.shop_api.entity.*;
import com.armando.shop_api.exception.NotFoundException;
import com.armando.shop_api.repository.*;
import com.armando.shop_api.service.OrderService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
public class OrderServiceImpl implements OrderService {

    private final OrderRepository orderRepository;
    private final UserRepository userRepository;
    private final ProductRepository productRepository;

    public OrderServiceImpl(
            OrderRepository orderRepository,
            UserRepository userRepository,
            ProductRepository productRepository
    ) {
        this.orderRepository = orderRepository;
        this.userRepository = userRepository;
        this.productRepository = productRepository;
    }

    @Override
    @Transactional
    public OrderResponse create(OrderCreateRequest req, String userEmail) {

        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new NotFoundException("User not found"));

        Order order = new Order();
        order.setUser(user);

        // items
        for (var itemReq : req.getItems()) {
            Product product = productRepository.findById(itemReq.getProductId())
                    .orElseThrow(() -> new NotFoundException("Product not found"));

            int qty = itemReq.getQuantity();

            // (opcional) validar stock
            if (product.getStock() < qty) {
                throw new IllegalArgumentException("Not enough stock for product: " + product.getName());
            }

            BigDecimal unitPrice = product.getPrice();
            BigDecimal subtotal = unitPrice.multiply(BigDecimal.valueOf(qty));

            OrderItem item = OrderItem.builder()
                    .order(order)
                    .product(product)
                    .quantity(qty)
                    .unitPrice(unitPrice)
                    .subtotal(subtotal)
                    .build();

            order.getItems().add(item);

            // (opcional) descontar stock
            product.setStock(product.getStock() - qty);
        }

        order.recalcTotal();

        Order saved = orderRepository.save(order);
        return map(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public List<OrderResponse> listMine(String userEmail) {
        // simple: filtrar en memoria (ok para reto). Si quieres pro: query por userEmail.
        return orderRepository.findAll().stream()
                .filter(o -> o.getUser().getEmail().equals(userEmail))
                .map(this::map)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public OrderResponse getMine(Long id, String userEmail) {
        Order o = orderRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Order not found"));

        if (!o.getUser().getEmail().equals(userEmail)) {
            throw new org.springframework.security.access.AccessDeniedException("Forbidden");
        }

        return map(o);
    }

    private OrderResponse map(Order o) {
        var items = o.getItems().stream()
                .map(i -> new OrderItemResponse(
                        i.getProduct().getId(),
                        i.getProduct().getName(),
                        i.getQuantity(),
                        i.getUnitPrice(),
                        i.getSubtotal()
                ))
                .toList();

        return new OrderResponse(
                o.getId(),
                o.getUser().getId(),
                o.getUser().getEmail(),
                o.getTotal(),
                items,
                o.getCreatedAt(),
                o.getCreatedBy(),
                o.getUpdatedAt(),
                o.getUpdatedBy()
        );
    }
}

