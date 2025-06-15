package com.lawyer.belawyer.service;

public interface PasswordResetService {
     void createAndSendToken(String email);
     void resetPassword(String rawToken, String newPassword);
}
