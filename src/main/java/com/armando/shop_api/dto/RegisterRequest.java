package com.armando.shop_api.dto;

import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class RegisterRequest {
    @NotBlank private String fullName;
    @Email @NotBlank private String email;
    @Size(min = 6) @NotBlank private String password;
}

