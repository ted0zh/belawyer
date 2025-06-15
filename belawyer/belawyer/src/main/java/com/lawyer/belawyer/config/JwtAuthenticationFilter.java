package com.lawyer.belawyer.config;

import com.lawyer.belawyer.service.serviceImpl.UserDetailsServiceImpl;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

@Component
@Slf4j
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    private final JwtService jwtService;
    private final UserDetailsServiceImpl userDetailsService;

    @Autowired
    public JwtAuthenticationFilter(JwtService jwtService, UserDetailsServiceImpl userDetailsService) {
        this.jwtService = jwtService;
        this.userDetailsService = userDetailsService;
    }

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request,
                                    @NonNull HttpServletResponse response,
                                    @NonNull FilterChain filterChain)
            throws ServletException, IOException {

        String path = request.getServletPath();
        log.info("Processing request for path: {}", path);

        if (path.startsWith("/api/v1/auth/register") ||
                path.startsWith("/api/v1/auth/login") ||
                path.startsWith("/api/v1/auth/refresh")) {
            log.info("Skipping JWT filter for auth endpoint: {}", path);
            filterChain.doFilter(request, response);
            return;
        }

        String authHeader = request.getHeader("Authorization");
        log.info("Authorization header: {}", authHeader != null ? authHeader.substring(0, Math.min(authHeader.length(), 20)) + "..." : "null");

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            log.warn("Missing or invalid Authorization header for path: {}", path);
            filterChain.doFilter(request, response);
            return;
        }try {
            String token = authHeader.substring(7);
            log.info("Extracted token (partial): {}", token.substring(0, Math.min(token.length(), 20)) + "...");

            String username = jwtService.extractUsername(token);
            log.info("Extracted username: {}", username);

            if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                log.info("Username extracted and authentication context is null. Proceeding with validation.");

                List<String> roles = jwtService.extractRoles(token);
                log.info("Extracted roles: {}", roles);

                if (jwtService.isTokenValid(token)) {
                    log.info("Token is valid.");
                    List<SimpleGrantedAuthority> authorities = roles.stream()
                            .map(SimpleGrantedAuthority::new)
                            .collect(Collectors.toList());
                    log.info("Created authorities: {}", authorities);

                    UsernamePasswordAuthenticationToken authToken =
                            new UsernamePasswordAuthenticationToken(username, null, authorities);
                    authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    SecurityContextHolder.getContext().setAuthentication(authToken);
                    log.info("Authentication context set for user: {}", username);
                } else {
                    log.warn("Token is invalid for user: {}", username);
                }
            } else if (username == null) {
                log.warn("Username could not be extracted from token for path: {}", path);
            } else {
                log.info("Username extracted, but authentication context is not null. User already authenticated?"); // Log if already authenticated
            }
        } catch (Exception e) {
            log.error("JWT authentication failed during processing for path {}: {}", path, e.getMessage(), e); // Log detailed error
            SecurityContextHolder.clearContext();
        }

        filterChain.doFilter(request, response);
    }
}

