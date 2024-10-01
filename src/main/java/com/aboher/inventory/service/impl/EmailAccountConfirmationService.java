package com.aboher.inventory.service.impl;

import com.aboher.inventory.enums.TokenType;
import com.aboher.inventory.model.ConfirmationToken;
import com.aboher.inventory.model.User;
import com.aboher.inventory.service.MessageSender;
import com.aboher.inventory.service.TokenBasedVerificationService;
import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.stereotype.Service;

@Data
@Service
public class EmailAccountConfirmationService implements TokenBasedVerificationService {
    private final int EXPIRATION_TIME_PERIOD_IN_MINUTES = 60 * 24;
    private final String FRONTEND_URL;
    private final String FRONTEND_USER_EMAIL_CONFIRMATION_PATH;
    private final ConfirmationTokenService confirmationTokenService;
    private final MessageSender<SimpleMailMessage> emailMessageSender;

    public EmailAccountConfirmationService(
            @Value("${frontend.url}") String FRONTEND_URL,
            @Value("${frontend.user-email-confirmation-handler-path}") String FRONTEND_USER_EMAIL_CONFIRMATION_PATH,
            ConfirmationTokenService confirmationTokenService,
            MessageSender<SimpleMailMessage> emailMessageSender) {
        this.FRONTEND_URL = FRONTEND_URL;
        this.FRONTEND_USER_EMAIL_CONFIRMATION_PATH = FRONTEND_USER_EMAIL_CONFIRMATION_PATH;
        this.emailMessageSender = emailMessageSender;
        this.confirmationTokenService = confirmationTokenService;
    }

    @Override
    public void sendMessageWithConfirmationToken(User user) {
        ConfirmationToken token = confirmationTokenService.createToken(user, TokenType.EMAIL_CONFIRMATION_TOKEN, EXPIRATION_TIME_PERIOD_IN_MINUTES);

        SimpleMailMessage mailMessage = new SimpleMailMessage();
        mailMessage.setTo(user.getEmail());
        mailMessage.setSubject("Complete Registration");
        mailMessage.setText(String.format("""
                To confirm your account, please click here:
                %s%s?token=%s

                Please, don't share this link with anyone.""", FRONTEND_URL, FRONTEND_USER_EMAIL_CONFIRMATION_PATH, token.getToken()));
        emailMessageSender.sendMessage(mailMessage);
    }

    @Override
    public User validateTokenAndReturnCorrespondingUser(String token) {
        ConfirmationToken confirmationToken = confirmationTokenService.validateToken(token, TokenType.EMAIL_CONFIRMATION_TOKEN);
        User user = confirmationToken.getUser();
        confirmationTokenService.deleteToken(confirmationToken);
        return user;
    }

    @Override
    public void deleteUserConfirmationTokenIfExists(User user) {
        confirmationTokenService.deleteUserConfirmationTokenIfExists(user, TokenType.EMAIL_CONFIRMATION_TOKEN);
    }
}
