package com.codewithmosh.store.services;

import com.codewithmosh.store.entities.User;
import com.codewithmosh.store.repositories.UserRepository;
import lombok.AllArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@AllArgsConstructor
@Service
public class AuthService {

    private final UserRepository userRepository;

    public User getCurrentUser() {
        // 1. Extract the principal from our Security Context
        var authentication = SecurityContextHolder.getContext().getAuthentication();
        // We cast the result of getPrincipal() to a string because
        // we stored the EMAIL of our users as the principle in the authentication object (in our JWT Auth Filter)
        var userId = (Long) authentication.getPrincipal();

        // 2. Find the user in our database
        return userRepository.findById(userId).orElse(null);
    }
}
