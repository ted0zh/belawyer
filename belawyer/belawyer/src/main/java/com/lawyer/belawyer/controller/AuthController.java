package com.lawyer.belawyer.controller;

import com.lawyer.belawyer.data.dto.ResetDto;
import com.lawyer.belawyer.data.dto.UserDto;
import com.lawyer.belawyer.service.AuthenticationResponse;
import com.lawyer.belawyer.service.AuthenticationService;
import com.lawyer.belawyer.service.PasswordResetService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("api/v1/auth")
public class AuthController {
    private final AuthenticationService service;
    private final PasswordResetService resetService;


    public AuthController(AuthenticationService service, PasswordResetService resetService) {
        this.service = service;
        this.resetService = resetService;
    }

    @PostMapping("/logout")
    public ResponseEntity<Map<String, String>> logout(@RequestHeader(value = "Authorization", required = false) String authHeader) {
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);
        }

        Authentication authentication = org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated() && !"anonymousUser".equals(authentication.getName())) {
            String username = authentication.getName();
            log.info("User {} is logging out", username);
        } else {
            log.info("Logout requested (no authenticated user in context)");
        }

        Map<String, String> response = new HashMap<>();
        response.put("message", "Logged out successfully");
        response.put("status", "success");
        return ResponseEntity.ok(response);
    }

    @PostMapping("/register")
    public ResponseEntity<AuthenticationResponse> register(@RequestBody UserDto dto) {
        return ResponseEntity.ok(service.register(dto));
    }

    @PostMapping("/login")
    public ResponseEntity<AuthenticationResponse> login(@RequestBody UserDto dto) {
        return ResponseEntity.ok(service.authenticate(dto));
    }
    @PostMapping("/request-reset")
    public ResponseEntity<?> requestReset(@RequestBody Map<String,String> body) {
        String email = body.get("email");
        resetService.createAndSendToken(email);
        return ResponseEntity.ok(Map.of("message", "Ако имейлът е валиден, ще получите инструкции."));
    }

    @PostMapping("/reset-password")
    public ResponseEntity<?> resetPassword(@RequestBody ResetDto dto) {
        resetService.resetPassword(dto.getToken(), dto.getNewPassword());
        return ResponseEntity.ok(Map.of("message", "Паролата е сменена успешно."));
    }
}