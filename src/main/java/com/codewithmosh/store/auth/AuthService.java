package com.codewithmosh.store.auth;

import com.codewithmosh.store.users.User;
import com.codewithmosh.store.users.UserRepository;
import com.krd.auth.dto.RegisterRequest;
import com.krd.auth.model.Role;
import com.krd.auth.security.JwtService;
import com.krd.auth.service.BaseUserService;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

/**
 * Authentication service extending BaseUserService from krd-spring-starters.
 *
 * Inherited from BaseUserService:
 * - register(RegisterRequest, HttpServletResponse)
 * - login(LoginRequest, HttpServletResponse)
 * - refreshAccessToken(String refreshToken, HttpServletResponse)
 * - logout(HttpServletResponse)
 *
 * Domain-specific additions:
 * - getCurrentUser() - get currently authenticated user
 */
@Service
public class AuthService extends BaseUserService<User> {

    public AuthService(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder,
            JwtService jwtService,
            AuthenticationManager authenticationManager,
            UserDetailsService userDetailsService
    ) {
        super(userRepository, passwordEncoder, jwtService, authenticationManager, userDetailsService);
    }

    /**
     * Get the currently authenticated user from the security context.
     */
    public User getCurrentUser() {
        var authentication = SecurityContextHolder.getContext().getAuthentication();
        var userId = (Long) authentication.getPrincipal();
        return userRepository.findById(userId).orElse(null);
    }

    /**
     * Create a User entity from a RegisterRequest.
     * This implementation is required by BaseUserService.
     */
    @Override
    protected User createUserFromRequest(RegisterRequest request) {
        User user = User.builder()
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .build();

        user.addRole(Role.ROLE_USER); // Add default user role
        return user;
    }
}
