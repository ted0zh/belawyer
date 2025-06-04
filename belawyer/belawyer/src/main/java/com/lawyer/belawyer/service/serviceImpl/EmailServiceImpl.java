//
//package com.lawyer.belawyer.service.serviceImpl;
//
//import com.lawyer.belawyer.service.EmailService;
//import org.springframework.beans.factory.annotation.Value;
//import org.springframework.mail.SimpleMailMessage;
//import org.springframework.mail.javamail.JavaMailSender;
//import org.springframework.stereotype.Service;
//
//@Service
//public class EmailServiceImpl implements EmailService {
//
//    private final JavaMailSender mailSender;
//
//    private String fromAddress;
//
//    public EmailServiceImpl(JavaMailSender mailSender) {
//        this.mailSender = mailSender;
//    }
//
//    @Override
//    public void sendSimpleMessage(String to, String subject, String text) {
//        SimpleMailMessage msg = new SimpleMailMessage();
//        msg.setFrom("me");
//        msg.setTo(to);
//        msg.setSubject(subject);
//        msg.setText(text);
//        mailSender.send(msg);
//    }
//}
