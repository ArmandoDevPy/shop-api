package com.armando.shop_api.service.impl;

import com.armando.shop_api.dto.*;
import com.armando.shop_api.entity.*;
import com.armando.shop_api.exception.BadRequestException;
import com.armando.shop_api.exception.ForbiddenException;
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

            if (qty <= 0) {
                throw new BadRequestException("Quantity must be > 0");
            }

            if (product.getStock() < qty) {
                throw new BadRequestException("Not enough stock for product: " + product.getName());
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

            // descontar stock
            product.setStock(product.getStock() - qty);
        }

        order.recalcTotal();

        Order saved = orderRepository.save(order);
        return map(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public List<OrderResponse> listMine(String userEmail) {
        return orderRepository.findByUserEmailOrderByIdDesc(userEmail).stream()
                .map(this::map)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public OrderResponse getMine(Long id, String userEmail) {
        Order o = orderRepository.findByIdAndUserEmail(id, userEmail)
                .orElseThrow(() -> new NotFoundException("Order not found"));
        return map(o);
    }

    @Override
    @Transactional
    public OrderResponse update(Long id, OrderCreateRequest req, String userEmail) {

        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Order not found"));

        // solo el dueño
        if (!order.getUser().getEmail().equals(userEmail)) {
            throw new ForbiddenException("Forbidden");
        }

        // 1) devolver stock anterior
        for (OrderItem oldItem : order.getItems()) {
            Product p = oldItem.getProduct();
            p.setStock(p.getStock() + oldItem.getQuantity());
        }

        // 2) borrar items anteriores (orphanRemoval=true)
        order.getItems().clear();

        // 3) agregar nuevos items
        for (var itemReq : req.getItems()) {

            Product product = productRepository.findById(itemReq.getProductId())
                    .orElseThrow(() -> new NotFoundException("Product not found"));

            int qty = itemReq.getQuantity();

            if (qty <= 0) {
                throw new BadRequestException("Quantity must be > 0");
            }

            if (product.getStock() < qty) {
                throw new BadRequestException("Not enough stock for product: " + product.getName());
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

            // descontar stock nuevo
            product.setStock(product.getStock() - qty);
        }

        // 4) recalcular total
        order.recalcTotal();

        Order saved = orderRepository.save(order);
        return map(saved);
    }

    @Override
    @Transactional
    public void delete(Long id, String userEmail) {

        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Order not found"));

        if (!order.getUser().getEmail().equals(userEmail)) {
            throw new ForbiddenException("Forbidden");
        }

        // devolver stock
        for (OrderItem item : order.getItems()) {
            Product p = item.getProduct();
            p.setStock(p.getStock() + item.getQuantity());
        }

        orderRepository.delete(order);
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
