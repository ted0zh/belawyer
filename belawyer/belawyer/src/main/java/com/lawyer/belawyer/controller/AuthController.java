package com.lawyer.belawyer.controller;

import com.lawyer.belawyer.data.dto.UserDto;
import com.lawyer.belawyer.service.AuthenticationResponse;
import com.lawyer.belawyer.service.AuthenticationService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.parameters.P;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("api/v1/auth")
public class AuthController {
    private final AuthenticationService service;


    public AuthController(AuthenticationService service) {
        this.service = service;
    }

    @PostMapping("/logout")
    public ResponseEntity<Map<String, String>> logout(@RequestHeader(value = "Authorization", required = false) String authHeader) {
        // Ако е подаден Authorization: Bearer <token>, логваме кой потребител е отплагирал,
        // но не държим blacklist в момента, просто връщаме "успех".
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);
            // Можем да извлечем потребителското име от токена, но тъй като JwtAuthenticationFilter вече е сетнал
            // SecurityContext, можем да вземем Authentication от контекста:
            // (по-долу логваме като anonymous само ако контекстът е празен)
            // Забележка: ако филтърът не е сетнал контекста (тоест токенът е невалиден),
            // все още ще попаднем в блока, но няма валиден потребител в контекста.
        }

        Authentication authentication = org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated() && !"anonymousUser".equals(authentication.getName())) {
            String username = authentication.getName();
            log.info("User {} is logging out", username);
        } else {
            log.info("Logout requested (no authenticated user in context)");
        }

        // Просто връщаме успех и фронтендът изтрива токена клиентско
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
}