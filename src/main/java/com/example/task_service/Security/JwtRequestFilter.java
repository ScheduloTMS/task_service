package com.example.task_service.Security;

import com.example.task_service.DTO.UserDTO;
import com.example.task_service.Util.JwtUtil;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;

@Component
public class JwtRequestFilter extends OncePerRequestFilter {

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private CustomUserDetailsService customUserDetailsService;

    @Autowired
    private RestTemplate restTemplate;

    @Value("${user.service.url}")
    private String userServiceUrl;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {

        final String authorizationHeader = request.getHeader("Authorization");

        System.out.println("Authorization Header: " + authorizationHeader);  // Log the authorization header

        // Skip filter for public endpoints
        if (request.getRequestURI().startsWith("/api/auth/")) {
            System.out.println("Public endpoint, skipping JWT filter.");
            chain.doFilter(request, response);
            return;
        }

        if (authorizationHeader == null || !authorizationHeader.startsWith("Bearer ")) {
            System.out.println("Invalid or missing Authorization header.");
            sendErrorResponse(response, HttpStatus.UNAUTHORIZED, "Missing or invalid Authorization header");
            return;
        }

        final String jwt = authorizationHeader.substring(7);  // Remove "Bearer " prefix
        final String email;

        try {
            email = jwtUtil.extractUsername(jwt);
            System.out.println("Extracted email from token: " + email);
            if (email == null) {
                System.out.println("Invalid JWT token, email is null.");
                sendErrorResponse(response, HttpStatus.UNAUTHORIZED, "Invalid JWT token");
                return;
            }
        } catch (JwtException e) {
            System.out.println("Invalid JWT token: " + e.getMessage());
            sendErrorResponse(response, HttpStatus.UNAUTHORIZED, "Invalid JWT token");
            return;
        }

        // Get user details from user service
        UserDTO user;
        try {
            user = fetchUserFromUserService(authorizationHeader);
            System.out.println("Fetched user from user service: " + user);
            if (user == null) {
                System.out.println("User not found.");
                sendErrorResponse(response, HttpStatus.UNAUTHORIZED, "User not found");
                return;
            }
        } catch (HttpClientErrorException e) {
            System.out.println("Error contacting user service: " + e.getMessage());
            sendErrorResponse(response, (HttpStatus) e.getStatusCode(), e.getResponseBodyAsString());
            return;
        } catch (Exception e) {
            System.out.println("Error contacting user service: " + e.getMessage());
            sendErrorResponse(response, HttpStatus.INTERNAL_SERVER_ERROR, "Error contacting user service");
            return;
        }

        // Check first login requirement
        if (user.isFirstLogin() && !isAllowedPath(request)) {
            System.out.println("User needs to change password.");
            sendErrorResponse(response, HttpStatus.FORBIDDEN, "Change your password to continue");
            return;
        }

        // Validate token and set authentication
        UserDetails userDetails = customUserDetailsService.loadUserByUsername(email);
        if (jwtUtil.validateToken(jwt, email)) {
            System.out.println("JWT Token validated successfully.");
            setAuthentication(userDetails, request, jwt);  // Now passing 3 arguments
        } else {
            System.out.println("Invalid or expired JWT token.");
            sendErrorResponse(response, HttpStatus.UNAUTHORIZED, "Invalid or expired token");
            return;
        }

        chain.doFilter(request, response);
    }

    private UserDTO fetchUserFromUserService(String authHeader) {
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", authHeader);
        HttpEntity<String> entity = new HttpEntity<>(headers);

        ResponseEntity<UserDTO> response = restTemplate.exchange(
                userServiceUrl + "/api/users/profile",
                HttpMethod.GET,
                entity,
                UserDTO.class
        );

        System.out.println("User fetched: " + response.getBody());
        return response.getBody();
    }

    private boolean isAllowedPath(HttpServletRequest request) {
        String path = request.getRequestURI();
        String method = request.getMethod();

        return path.equals("/api/auth/login") ||
                (path.equals("/api/users/profile") && method.equals("PUT")) ||
                (path.equals("/api/messages/history") && method.equals("GET"));
    }

    // In JwtRequestFilter.java
    private void setAuthentication(UserDetails userDetails, HttpServletRequest request, String jwt) {
        try {
            String role = jwtUtil.extractRole(jwt);
            System.out.println("Extracted role from token: " + role);  // Log the extracted role
            UsernamePasswordAuthenticationToken authentication =
                    new UsernamePasswordAuthenticationToken(
                            userDetails,
                            null,
                            Collections.singleton(new SimpleGrantedAuthority("ROLE_" + role)) // Add prefix
                    );
            authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
            SecurityContextHolder.getContext().setAuthentication(authentication);

            // Debug log
            System.out.println("Authentication set with authorities: " +
                    SecurityContextHolder.getContext().getAuthentication().getAuthorities());
        } catch (JwtException e) {
            throw new RuntimeException("Failed to extract role from token", e);
        }
    }

    private void sendErrorResponse(HttpServletResponse response, HttpStatus status, String message)
            throws IOException {
        response.setStatus(status.value());
        response.setContentType("application/json");
        response.getWriter().write(String.format(
                "{\"status\":%d,\"message\":\"%s\",\"body\":null}",
                status.value(),
                message
        ));
    }
}
