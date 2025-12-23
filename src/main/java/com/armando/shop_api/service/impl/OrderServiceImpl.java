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
import com.armando.shop_api.dto.OrderItemCreateRequest;

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

    // ======================================================
    // CREATE ORDER
    // ======================================================
    @Override
    @Transactional
    public OrderResponse create(OrderCreateRequest req, String userEmail) {

        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new NotFoundException("User not found"));

        Order order = new Order();
        order.setUser(user);

        for (OrderItemCreateRequest itemReq : req.getItems()) {

            Product product = productRepository.findById(itemReq.getProductId())
                    .orElseThrow(() -> new NotFoundException("Product not found"));

            int qty = itemReq.getQuantity();

            if (product.getStock() < qty) {
                throw new BadRequestException(
                        "Not enough stock for product: " + product.getName());
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

    // ======================================================
    // UPDATE ORDER
    // ======================================================
    @Override
    @Transactional
    public OrderResponse update(Long id, OrderCreateRequest req, String userEmail) {

        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Order not found"));

        if (!order.getUser().getEmail().equals(userEmail)) {
            throw new ForbiddenException("You cannot modify this order");
        }

        // 1️⃣ devolver stock anterior
        for (OrderItem oldItem : order.getItems()) {
            Product product = oldItem.getProduct();
            product.setStock(product.getStock() + oldItem.getQuantity());
        }

        // 2️⃣ limpiar items
        order.getItems().clear();

        // 3️⃣ agregar nuevos items
        for (OrderItemCreateRequest itemReq : req.getItems()) {

            Product product = productRepository.findById(itemReq.getProductId())
                    .orElseThrow(() -> new NotFoundException("Product not found"));

            int qty = itemReq.getQuantity();

            if (product.getStock() < qty) {
                throw new BadRequestException(
                        "Not enough stock for product: " + product.getName());
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
            product.setStock(product.getStock() - qty);
        }

        order.recalcTotal();

        return map(orderRepository.save(order));
    }

    // ======================================================
    // LIST MY ORDERS
    // ======================================================
    @Override
    @Transactional(readOnly = true)
    public List<OrderResponse> listMine(String userEmail) {
        return orderRepository.findAll().stream()
                .filter(o -> o.getUser().getEmail().equals(userEmail))
                .map(this::map)
                .toList();
    }

    // ======================================================
    // GET MY ORDER
    // ======================================================
    @Override
    @Transactional(readOnly = true)
    public OrderResponse getMine(Long id, String userEmail) {

        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Order not found"));

        if (!order.getUser().getEmail().equals(userEmail)) {
            throw new ForbiddenException("You cannot access this order");
        }

        return map(order);
    }

    // ======================================================
    // MAPPER
    // ======================================================
    private OrderResponse map(Order order) {

        List<OrderItemResponse> items = order.getItems().stream()
                .map(i -> new OrderItemResponse(
                        i.getProduct().getId(),
                        i.getProduct().getName(),
                        i.getQuantity(),
                        i.getUnitPrice(),
                        i.getSubtotal()
                ))
                .toList();

        return new OrderResponse(
                order.getId(),
                order.getUser().getId(),
                order.getUser().getEmail(),
                order.getTotal(),
                items,
                order.getCreatedAt(),
                order.getCreatedBy(),
                order.getUpdatedAt(),
                order.getUpdatedBy()
        );
    }
}
