package com.armando.shop_api.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter @Setter
public class OrderCreateRequest {

    @NotEmpty(message = "items is required")
    private List<@Valid OrderItemCreateRequest> items;
}

