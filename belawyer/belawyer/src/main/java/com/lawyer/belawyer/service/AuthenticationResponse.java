package com.lawyer.belawyer.service;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@Getter
@NoArgsConstructor
public class AuthenticationResponse {
    private String token;
    private String username;
    private String role;
}
