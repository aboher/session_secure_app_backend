package com.aboher.inventory.service.impl;

import com.aboher.inventory.service.MessageSender;
import lombok.RequiredArgsConstructor;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class EmailMessageSender implements MessageSender<SimpleMailMessage> {

    private final JavaMailSender javaMailSender;

    @Override
    @Async
    public void sendMessage(SimpleMailMessage message) {
        javaMailSender.send(message);
    }
}
