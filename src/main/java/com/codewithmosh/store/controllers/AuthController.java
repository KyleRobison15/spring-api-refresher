package com.codewithmosh.store.controllers;

import com.codewithmosh.store.config.JwtConfig;
import com.codewithmosh.store.dtos.JwtResponse;
import com.codewithmosh.store.dtos.LoginRequest;
import com.codewithmosh.store.dtos.UserDto;
import com.codewithmosh.store.mappers.UserMapper;
import com.codewithmosh.store.repositories.UserRepository;
import com.codewithmosh.store.services.JwtService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@AllArgsConstructor
@RestController
@RequestMapping("/auth")
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final JwtConfig jwtConfig;
    private final UserRepository userRepository;
    private final UserMapper userMapper;

    @PostMapping("/login")
    public ResponseEntity<JwtResponse> login(@Valid @RequestBody LoginRequest request, HttpServletResponse response) {

        // Authenticate the user using our Authentication Manager
            // which uses our UserDetailsService implementation to find a user and verify their password
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getEmail(),
                        request.getPassword()
                )
        );

        // Get the user from our database so we can generate the JWT for them
        var user = userRepository.findByEmail(request.getEmail()).orElseThrow();

        // Once the user is authenticated, generate an Access Token and Refresh Token
        var accessToken = jwtService.generateAccessToken(user);
        var refreshToken = jwtService.generateRefreshToken(user);

        var cookie = new Cookie("refreshToken", refreshToken);
        cookie.setHttpOnly(true); // Ensures this cookie CANNOT be accessed by JavaScript
        cookie.setPath("/auth/refresh"); // Ensures the only time this cookie is set is when a request is made to the /auth/refresh endpoint
        cookie.setMaxAge(jwtConfig.getRefreshTokenExpiration()); //7d - this cookie should expire the same time the refresh token does
        cookie.setSecure(true); // Ensures this cookie can only be sent over HTTPS (and not unsecured HTTP)
        response.addCookie(cookie); // Add the cookie to the response

        // Return the access token in the response body
        return ResponseEntity.ok(new JwtResponse(accessToken));
    }

    @PostMapping("/refresh")
    public ResponseEntity<JwtResponse> refresh(@CookieValue(value = "refreshToken") String refreshToken) {

        if(!jwtService.validateToken(refreshToken)){
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        var userId = jwtService.getUserIdFromToken(refreshToken);
        var user = userRepository.findById(userId).orElseThrow();
        var accessToken = jwtService.generateAccessToken(user);

        return ResponseEntity.ok(new JwtResponse(accessToken));
    }

    @GetMapping("/me")
    public ResponseEntity<UserDto> me(){
        // 1. Extract the principal from our Security Context
        var authentication = SecurityContextHolder.getContext().getAuthentication();
        // We cast the result of getPrincipal() to a string because
            // we stored the EMAIL of our users as the principle in the authentication object (in our JWT Auth Filter)
        var userId = (Long) authentication.getPrincipal();

        // 2. Find the user in our database
        var user = userRepository.findById(userId).orElse(null);
        if(user == null){
            return ResponseEntity.notFound().build();
        }

        // 3. Map the user in our DB to the UserDto and return it
        var userDto = userMapper.toDto(user);
        return ResponseEntity.ok(userDto);
    }

    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<Void> handleBadCredentialsException(){
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
    }

}
