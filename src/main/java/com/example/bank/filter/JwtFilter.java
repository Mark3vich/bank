package com.example.bank.filter;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import com.example.bank.service.JwtService;
import com.example.bank.service.UserService;

import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.UnsupportedJwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.AllArgsConstructor;

@Component
@AllArgsConstructor
public class JwtFilter extends OncePerRequestFilter {
    private static final Logger jwtFilterLogger = LoggerFactory.getLogger(JwtFilter.class);

    private final JwtService jwtService;
    private final UserService userService;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain)
            throws ServletException, IOException {

        final String authHeader = request.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            jwtFilterLogger.debug("No Authorization header or not a Bearer token - skipping JWT validation");
            filterChain.doFilter(request, response);
            return;
        }

        final String token = authHeader.substring(7);
        try {
            jwtFilterLogger.debug("Validating token for path: {}", request.getRequestURI());
            final String identifier = jwtService.extractIdentifier(token);
            jwtFilterLogger.debug("Extracted identifier from token: {}", identifier);

            if (identifier != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                UserDetails userDetails = userService.loadUserByUsername(identifier);
                jwtFilterLogger.debug("Loaded UserDetails for identifier: {}", identifier);

                if (jwtService.isValid(token, userDetails)) {
                    logger.debug("Token is valid, setting authentication context");
                    UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                            userDetails,
                            null,
                            userDetails.getAuthorities());

                    authToken.setDetails(
                            new WebAuthenticationDetailsSource().buildDetails(request));

                    SecurityContextHolder.getContext().setAuthentication(authToken);
                } else {
                    jwtFilterLogger.debug("Token validation failed for identifier: {}", identifier);
                }
            } else {
                logger.debug("Authentication context already set or identifier is null");
            }
        } catch (ExpiredJwtException | UnsupportedJwtException | MalformedJwtException | IllegalArgumentException
                | UsernameNotFoundException e) {
            jwtFilterLogger.error("Error validating JWT token: {}", e.getMessage(), e);
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Invalid token: " + e.getMessage());
            return;
        }

        filterChain.doFilter(request, response);
    }
}
