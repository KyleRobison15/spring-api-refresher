package com.codewithmosh.store.services;

import com.codewithmosh.store.config.JwtConfig;
import com.codewithmosh.store.entities.User;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Date;

@AllArgsConstructor
@Service
public class JwtService {

    // Inject the configurations for our tokens
    private final JwtConfig config;

    public String generateAccessToken(User user) {
        return generateToken(user, config.getAccessTokenExpiration());
    }

    public String generateRefreshToken(User user) {
        return generateToken(user, config.getRefreshTokenExpiration());
    }

    private String generateToken(User user, long tokenExpiration) {
        return Jwts.builder()

                // Set the "sub" property of the JWT's payload to the user's id
                .subject(user.getId().toString())

                // Add the user's name and email as claims in the JWT
                // This will allow us to get this info from the token instead of querying the DB to get the user details
                .claim("name", user.getName())
                .claim("email", user.getEmail())

                // Set the "iat" property of the JWT's payload to the current date
                .issuedAt(new Date())

                // Determines how long this token will be valid - in this case for 1 day
                .expiration(new Date(System.currentTimeMillis() + 1000 * tokenExpiration))

                // Set the secret and signing algorithm that will be used to generate the signature for our tokens
                .signWith(config.getSecretKey())

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
    public Long getUserIdFromToken(String token) {
        return Long.valueOf(getClaims(token).getSubject());
    }

    private Claims getClaims(String token) {
        return Jwts.parser()
                .verifyWith(config.getSecretKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

}
