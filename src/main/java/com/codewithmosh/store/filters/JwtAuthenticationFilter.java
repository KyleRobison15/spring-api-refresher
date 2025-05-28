package com.codewithmosh.store.filters;

import com.codewithmosh.store.auth.JwtService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.AllArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

@AllArgsConstructor
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {

        // Extract the Authorization header from the request
        var authHeader = request.getHeader("Authorization");

        // Ensure the Authorization header is properly formed
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            // If it is not properly formed, we just move on to the next filter in the filter chain
            // At this point, Spring Security will pick it up, and if the target resource/endpoint is protected, a 403 error will be returned
            filterChain.doFilter(request, response);
            return;
        }

        var token = authHeader.replace("Bearer ", "");
        var jwt = jwtService.parseToken(token);

        if(jwt == null || jwt.isExpired()) {
            filterChain.doFilter(request, response);
            return;
        }

        // At this point, we have a valid token
        // Now we need to tell spring that the user is authenticated and should be allowed access to protected resources/endpoints
        var authentication = new UsernamePasswordAuthenticationToken(
                jwt.getUserId(),
                null,
                List.of(new SimpleGrantedAuthority("ROLE_" + jwt.getRole()))
        );

        authentication.setDetails(
                new WebAuthenticationDetailsSource().buildDetails(request) // Attaches additional metadata of the request to the authentication details
        );

        // Store the user's authentication details in the security context
        SecurityContextHolder.getContext().setAuthentication(authentication);

        filterChain.doFilter(request, response);
    }
}
