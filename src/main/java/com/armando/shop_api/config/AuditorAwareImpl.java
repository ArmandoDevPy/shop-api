package com.armando.shop_api.config;

import org.springframework.data.domain.AuditorAware;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Optional;

public class AuditorAwareImpl implements AuditorAware<String> {

    @Override
    public Optional<String> getCurrentAuditor() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        if (auth == null || !auth.isAuthenticated()) {
            return Optional.of("system");
        }

        String name = auth.getName(); // en tu caso: email
        if (name == null || name.isBlank() || "anonymousUser".equalsIgnoreCase(name)) {
            return Optional.of("system");
        }

        return Optional.of(name);
    }
}
