package com.lawyer.belawyer.data.dto;

import lombok.Data;

@Data
public class ResetDto {
    private String token;
    private String newPassword;
}
