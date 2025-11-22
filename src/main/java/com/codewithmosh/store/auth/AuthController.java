package com.codewithmosh.store.auth;

import com.codewithmosh.store.users.User;
import com.codewithmosh.store.users.UserDto;
import com.codewithmosh.store.users.UserMapper;
import com.krd.auth.controller.BaseAuthController;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Authentication controller extending BaseAuthController from krd-spring-starters.
 *
 * Inherited endpoints from BaseAuthController:
 * - POST /register - Register a new user
 * - POST /login - Login user
 * - POST /refresh - Refresh access token from cookie
 * - POST /logout - Logout user
 *
 * Domain-specific endpoints:
 * - GET /me - Get current user information
 */
@RestController
@RequestMapping("/auth")
@Tag(name = "Auth")
public class AuthController extends BaseAuthController<User> {

    private final UserMapper userMapper;

    public AuthController(AuthService authService, UserMapper userMapper) {
        super(authService);
        this.userMapper = userMapper;
    }

    /**
     * Get current authenticated user information.
     * GET /auth/me
     */
    @GetMapping("/me")
    public ResponseEntity<UserDto> me() {
        var user = ((AuthService) userService).getCurrentUser();
        if (user == null) {
            return ResponseEntity.notFound().build();
        }

        var userDto = userMapper.toDto(user);
        return ResponseEntity.ok(userDto);
    }
}
