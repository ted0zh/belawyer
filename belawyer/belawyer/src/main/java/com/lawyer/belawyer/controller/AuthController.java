package com.lawyer.belawyer.controller;

import com.lawyer.belawyer.data.dto.UserDto;
import com.lawyer.belawyer.service.AuthenticationResponse;
import com.lawyer.belawyer.service.AuthenticationService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.parameters.P;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("api/v1/auth")
public class AuthController {
    private final AuthenticationService service;


    public AuthController(AuthenticationService service) {
        this.service = service;
    }

    @PostMapping("/register")
    public ResponseEntity<AuthenticationResponse> register(@RequestBody UserDto dto){
        return ResponseEntity.ok(service.register(dto));
    }
    @PostMapping("/login")
    public ResponseEntity<AuthenticationResponse> login(@RequestBody UserDto dto){
        return ResponseEntity.ok(service.authenticate(dto));
    }
}
