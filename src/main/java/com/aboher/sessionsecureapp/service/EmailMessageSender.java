package com.aboher.sessionsecureapp.service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class EmailMessageSender implements MessageSender<SimpleMailMessage> {

    @Value("${spring.mail.username}")
    private String smtpEmail;

    private final JavaMailSender javaMailSender;

    @Override
    @Async
    public void sendMessage(SimpleMailMessage message) {
        message.setFrom(smtpEmail);
        javaMailSender.send(message);
    }
}
