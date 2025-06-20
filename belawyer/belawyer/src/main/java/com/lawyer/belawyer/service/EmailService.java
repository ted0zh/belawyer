package com.lawyer.belawyer.service;

public interface EmailService {
    void sendSimpleMessage(String to, String subject, String text);
}
