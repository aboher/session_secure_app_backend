package com.aboher.inventory.service.impl;

import com.aboher.inventory.enums.TokenType;
import com.aboher.inventory.model.ConfirmationToken;
import com.aboher.inventory.model.User;
import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.stereotype.Service;

@Data
@Service
public class EmailConfirmationService {
    private final int EXPIRATION_TIME_PERIOD_IN_MINUTES = 60 * 24;
    private final String FRONTEND_URL;
    private final String FRONTEND_USER_EMAIL_CONFIRMATION_PATH;
    private final EmailService emailService;
    private final ConfirmationTokenService confirmationTokenService;

    public EmailConfirmationService(
            @Value("${frontend.url}") String FRONTEND_URL,
            @Value("${frontend.user-email-confirmation-handler-path}") String FRONTEND_USER_EMAIL_CONFIRMATION_PATH,
            EmailService emailService,
            ConfirmationTokenService confirmationTokenService) {
        this.FRONTEND_URL = FRONTEND_URL;
        this.FRONTEND_USER_EMAIL_CONFIRMATION_PATH = FRONTEND_USER_EMAIL_CONFIRMATION_PATH;
        this.emailService = emailService;
        this.confirmationTokenService = confirmationTokenService;
    }

    public void sendEmailWithConfirmationToken(User user) {
        ConfirmationToken token = confirmationTokenService.createToken(user, TokenType.EMAIL_CONFIRMATION_TOKEN, EXPIRATION_TIME_PERIOD_IN_MINUTES);

        SimpleMailMessage mailMessage = new SimpleMailMessage();
        mailMessage.setTo(user.getEmail());
        mailMessage.setSubject("Complete Registration");
        mailMessage.setText(String.format("""
                To confirm your account, please click here:
                %s%s?token=%s

                Please, don't share this link with anyone.""", FRONTEND_URL, FRONTEND_USER_EMAIL_CONFIRMATION_PATH, token.getToken()));
        emailService.sendEmail(mailMessage);
    }

    public User validateTokenAndReturnCorrespondingUser(String token) {
        ConfirmationToken confirmationToken = confirmationTokenService.validateToken(token, TokenType.EMAIL_CONFIRMATION_TOKEN);
        User user = confirmationToken.getUser();
        confirmationTokenService.deleteToken(confirmationToken);
        return user;
    }

    public void deleteUserConfirmationTokenIfExists(User user) {
        confirmationTokenService.deleteUserConfirmationTokenIfExists(user, TokenType.EMAIL_CONFIRMATION_TOKEN);
    }
}
