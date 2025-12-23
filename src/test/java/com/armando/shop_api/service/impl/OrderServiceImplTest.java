package com.armando.shop_api.service.impl;

import com.armando.shop_api.dto.OrderCreateRequest;
import com.armando.shop_api.dto.OrderItemCreateRequest;
import com.armando.shop_api.dto.OrderResponse;
import com.armando.shop_api.entity.Product;
import com.armando.shop_api.entity.User;
import com.armando.shop_api.exception.BadRequestException;
import com.armando.shop_api.exception.ForbiddenException;
import com.armando.shop_api.exception.NotFoundException;
import com.armando.shop_api.repository.OrderRepository;
import com.armando.shop_api.repository.ProductRepository;
import com.armando.shop_api.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderServiceImplTest {

    @Mock
    OrderRepository orderRepository;

    @Mock
    UserRepository userRepository;

    @Mock
    ProductRepository productRepository;

    @InjectMocks
    OrderServiceImpl orderService;

    private User user;
    private Product laptop;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setId(4L);
        user.setFullName("Pedro User");
        user.setEmail("pedro@mail.com");

        laptop = new Product();
        laptop.setId(3L);
        laptop.setName("Laptop");
        laptop.setPrice(new BigDecimal("2500.00"));
        laptop.setStock(10);
    }

    @Test
    void create_ok_calculatesTotal_and_decrementsStock() {
        // arrange
        when(userRepository.findByEmail("pedro@mail.com")).thenReturn(Optional.of(user));
        when(productRepository.findById(3L)).thenReturn(Optional.of(laptop));

        // al guardar orden, simula que ya tiene ID
        when(orderRepository.save(any())).thenAnswer(invocation -> {
            var order = invocation.getArgument(0, com.armando.shop_api.entity.Order.class);
            order.setId(99L);
            return order;
        });

        OrderCreateRequest req = new OrderCreateRequest();
        req.setItems(List.of(item(3L, 2))); // 2 laptops => 5000

        // act
        OrderResponse response = orderService.create(req, "pedro@mail.com");

        // assert (OrderResponse es record => id(), total(), items())
        assertEquals(99L, response.id());
        assertEquals(new BigDecimal("5000.00"), response.total());
        assertEquals(1, response.items().size());
        assertEquals(3L, response.items().get(0).productId());
        assertEquals(2, response.items().get(0).quantity());

        // stock descontado
        assertEquals(8, laptop.getStock());

        verify(orderRepository, times(1)).save(any());
    }

    @Test
    void create_userNotFound_throwsNotFound() {
        when(userRepository.findByEmail("x@mail.com")).thenReturn(Optional.empty());

        OrderCreateRequest req = new OrderCreateRequest();
        req.setItems(List.of(item(3L, 1)));

        assertThrows(NotFoundException.class, () -> orderService.create(req, "x@mail.com"));
        assertTrue(true);
        verify(orderRepository, never()).save(any());
    }

    @Test
    void create_productNotFound_throwsNotFound() {
        when(userRepository.findByEmail("pedro@mail.com")).thenReturn(Optional.of(user));
        when(productRepository.findById(3L)).thenReturn(Optional.empty());

        OrderCreateRequest req = new OrderCreateRequest();
        req.setItems(List.of(item(3L, 1)));

        assertThrows(NotFoundException.class, () -> orderService.create(req, "pedro@mail.com"));
        verify(orderRepository, never()).save(any());
    }

    @Test
    void create_qtyZero_throwsBadRequest() {
        when(userRepository.findByEmail("pedro@mail.com")).thenReturn(Optional.of(user));
        when(productRepository.findById(3L)).thenReturn(Optional.of(laptop));

        OrderCreateRequest req = new OrderCreateRequest();
        req.setItems(List.of(item(3L, 0)));

        assertThrows(BadRequestException.class, () -> orderService.create(req, "pedro@mail.com"));
        verify(orderRepository, never()).save(any());
    }

    @Test
    void update_notOwner_throwsForbidden() {
        // si necesitas este test, debe existir el método update en tu servicio
        // y debe buscar la orden por id.

        var existingOrder = new com.armando.shop_api.entity.Order();
        existingOrder.setId(1L);

        var anotherUser = new User();
        anotherUser.setId(99L);
        anotherUser.setEmail("other@mail.com");
        existingOrder.setUser(anotherUser);

        when(orderRepository.findById(1L)).thenReturn(Optional.of(existingOrder));

        OrderCreateRequest req = new OrderCreateRequest();
        req.setItems(List.of(item(3L, 1)));

        assertThrows(ForbiddenException.class, () -> orderService.update(1L, req, "pedro@mail.com"));
        verify(orderRepository, never()).save(any());
    }

    private static OrderItemCreateRequest item(Long productId, int quantity) {
        OrderItemCreateRequest it = new OrderItemCreateRequest();
        it.setProductId(productId);
        it.setQuantity(quantity);
        return it;
    }
}
