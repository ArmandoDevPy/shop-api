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
        if (authentication == null) {
            authentication = SecurityContextHolder.getContext().getAuthentication();
        }

        Map<String, Object> res = new HashMap<>();
        res.put("authNull", authentication == null);

        if (authentication == null)
            return res;

        res.put("name", authentication.getName());
        res.put("authorities", authentication.getAuthorities());
        res.put("principalClass", authentication.getPrincipal() != null
                ? authentication.getPrincipal().getClass().getName()
                : null);

        Object principal = authentication.getPrincipal();

        if (principal instanceof com.armando.shop_api.security.UserPrincipal p) {
            res.put("id", p.getId());
            res.put("fullName", p.getFullName());
            res.put("email", p.getEmail());
            res.put("role", p.getRole());
        }

        return res;
    }

}
