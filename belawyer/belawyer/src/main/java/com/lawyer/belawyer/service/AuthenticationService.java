package com.lawyer.belawyer.service;

import com.lawyer.belawyer.config.JwtService;
import com.lawyer.belawyer.data.dto.UserDto;
import com.lawyer.belawyer.data.entity.User;
import com.lawyer.belawyer.service.serviceImpl.UserDetailsServiceImpl;
import com.lawyer.belawyer.service.serviceImpl.UserServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class AuthenticationService {

    private final UserDetailsServiceImpl userDetailsService;
    private final UserServiceImpl userService;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;

    @Autowired
    public AuthenticationService(UserDetailsServiceImpl userDetailsService, UserServiceImpl userService, JwtService jwtService, AuthenticationManager authenticationManager) {
        this.userDetailsService = userDetailsService;
        this.userService = userService;
        this.jwtService = jwtService;
        this.authenticationManager = authenticationManager;
    }

    public AuthenticationResponse register(UserDto request) {
        try {
            User user = userService.createUser(request);

            // Load user details to get proper authorities
            UserDetails userDetails = userDetailsService.loadUserByUsername(user.getUsername());
            String token = jwtService.generateToken(userDetails);

            log.info("User registered successfully: {}", user.getUsername());
            return new AuthenticationResponse(token, user.getUsername(), user.getRole().name());
        } catch (Exception e) {
            log.error("Registration failed for user: {}", request.getUsername(), e);
            throw new RuntimeException("Registration failed: " + e.getMessage());
        }
    }

    public AuthenticationResponse authenticate(UserDto request) {
        try {
            // Authenticate the user
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            request.getUsername(),
                            request.getPassword()

                    )
            );

            // Get user details from the authenticated user
            UserDetails userDetails = (UserDetails) authentication.getPrincipal();
            User user = userService.findByUsername(request.getUsername());

            // Generate JWT token with proper roles
            String jwtToken = jwtService.generateToken(userDetails);

            log.info("User authenticated successfully: {}", user.getUsername());
            return new AuthenticationResponse(jwtToken, user.getUsername(), user.getRole().name());

        } catch (BadCredentialsException e) {
            log.warn("Authentication failed for user: {} - Invalid credentials", request.getUsername());
            throw new BadCredentialsException("Invalid username or password");
        } catch (AuthenticationException e) {
            log.error("Authentication failed for user: {}", request.getUsername(), e);
            throw new RuntimeException("Authentication failed: " + e.getMessage());
        }
    }
}
