package com.codewithmosh.store.services;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Date;

@Service
public class JwtService {

    @Value("${spring.jwt.secret}")
    private String secret;

    // Method for generating JSON Web Tokens
    public String generateToken(String email) {

        // 1 Day expiration period (tokens will be valid for one day)
        final long tokenExpiration = 86400;

        return Jwts.builder()

                 // Set the "sub" property of the JWT's payload to the user's email
                .subject(email)

                // Set the "iat" property of the JWT's payload to the current date
                .issuedAt(new Date())

                // Determines how long this token will be valid - in this case for 1 day
                .expiration(new Date(System.currentTimeMillis() + 1000 * tokenExpiration))

                // Set the secret and signing algorithm that will be used to generate the signature for our tokens
                .signWith(Keys.hmacShaKeyFor(secret.getBytes()))

                // Use the information provided above and generate the JWT string
                .compact();
    }

    public boolean validateToken(String token) {
        try {
            return getClaims(token).getExpiration().after(new Date());
        } catch (JwtException e) {
            return false;
        }
    }

    // We use this method in our JWT Authentication Filter
    public String getEmailFromToken(String token) {
        return getClaims(token).getSubject();
    }

    private Claims getClaims(String token) {
        return Jwts.parser()
                .verifyWith(Keys.hmacShaKeyFor(secret.getBytes()))
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

}
