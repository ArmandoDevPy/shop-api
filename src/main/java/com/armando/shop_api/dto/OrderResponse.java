package com.armando.shop_api.dto;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

public record OrderResponse(
        Long id,
        Long userId,
        String userEmail,
        BigDecimal total,
        List<OrderItemResponse> items,
        Instant createdAt,
        String createdBy,
        Instant updatedAt,
        String updatedBy
) {}
