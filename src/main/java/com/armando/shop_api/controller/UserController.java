package com.armando.shop_api.controller;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
public class UserController {

    @GetMapping("/me")
    public Map<String, Object> me(Authentication authentication) {
        // Por si Spring no inyecta bien Authentication
        if (authentication == null) {
            authentication = SecurityContextHolder.getContext().getAuthentication();
        }

        Map<String, Object> res = new HashMap<>();
        res.put("authNull", authentication == null);

        if (authentication != null) {
            res.put("name", authentication.getName());
            res.put("authorities", authentication.getAuthorities()); // puede ser null sin reventar
            res.put("principalClass", authentication.getPrincipal() != null ? authentication.getPrincipal().getClass().getName() : null);
        }

        return res;
    }
}
