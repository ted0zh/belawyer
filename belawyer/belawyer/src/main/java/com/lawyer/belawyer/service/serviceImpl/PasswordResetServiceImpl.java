package com.lawyer.belawyer.service.serviceImpl;

import com.lawyer.belawyer.data.entity.PasswordResetToken;
import com.lawyer.belawyer.data.entity.User;
import com.lawyer.belawyer.repository.PasswordResetTokenRepository;
import com.lawyer.belawyer.repository.UserRepository;
import com.lawyer.belawyer.service.PasswordResetService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PasswordResetServiceImpl implements PasswordResetService {

    private final UserRepository userRepo;
    private final PasswordResetTokenRepository tokenRepo;
    private final JavaMailSender mailSender;
    private final PasswordEncoder passwordEncoder;


    @Transactional
    public void createAndSendToken(String email) {
        User user = userRepo.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("Не съществува такъв имейл"));

        String token = UUID.randomUUID().toString() + "-" + RandomStringUtils.randomAlphanumeric(20);
        Instant expires = Instant.now().plus(1, ChronoUnit.HOURS);

        PasswordResetToken prt = new PasswordResetToken();
        prt.setUser(user);
        prt.setToken(passwordEncoder.encode(token));
        prt.setExpiresAt(expires);
        tokenRepo.save(prt);

        String resetUrl = "https://localhost:3000/reset-password?token=" + URLEncoder.encode(token, StandardCharsets.UTF_8);

        SimpleMailMessage msg = new SimpleMailMessage();
        msg.setFrom("no-reply@somemail.com");
        msg.setTo(user.getEmail());
        msg.setSubject("Reset your password");
        msg.setText("Кликнете тук, за да нулирате паролата: " + resetUrl + "\n\nЛинкът изтича след 1 час.");
        mailSender.send(msg);
    }

    @Transactional
    public void resetPassword(String rawToken, String newPassword) {
        PasswordResetToken prt = tokenRepo.findAll().stream()
                .filter(t -> passwordEncoder.matches(rawToken, t.getToken()))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Невалиден или изтекъл линк"));

        if (prt.getUsedAt() != null || prt.getExpiresAt().isBefore(Instant.now())) {
            throw new IllegalArgumentException("Линкът е невалиден или изтекъл");
        }

        User user = prt.getUser();
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepo.save(user);

        prt.setUsedAt(Instant.now());
        tokenRepo.save(prt);

        tokenRepo.deleteAllExpiredSince(Instant.now().minus(1, ChronoUnit.DAYS));
    }
}
