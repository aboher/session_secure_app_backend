package com.aboher.inventory.service.impl;

import com.aboher.inventory.enums.TokenType;
import com.aboher.inventory.model.ConfirmationToken;
import com.aboher.inventory.model.User;
import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.stereotype.Service;

@Service
@Data
public class PasswordChangeService {
    private final int EXPIRATION_TIME_PERIOD_IN_MINUTES = 10;
    private final String FRONTEND_URL;
    private final String FRONTEND_USER_PASSWORD_CHANGE_PATH;
    private final ConfirmationTokenService confirmationTokenService;
    private final EmailService emailService;

    public PasswordChangeService(
            @Value("${frontend.url}") String FRONTEND_URL,
            @Value("${frontend.user-password-change-path}") String FRONTEND_USER_PASSWORD_CHANGE_PATH,
            ConfirmationTokenService confirmationTokenService,
            EmailService emailService) {
        this.FRONTEND_URL = FRONTEND_URL;
        this.FRONTEND_USER_PASSWORD_CHANGE_PATH = FRONTEND_USER_PASSWORD_CHANGE_PATH;
        this.confirmationTokenService = confirmationTokenService;
        this.emailService = emailService;
    }

    public void sendEmailWithConfirmationToken(User user) {
        ConfirmationToken token = confirmationTokenService.createToken(user, TokenType.PASSWORD_CHANGE_TOKEN, EXPIRATION_TIME_PERIOD_IN_MINUTES);

        SimpleMailMessage mailMessage = new SimpleMailMessage();
        mailMessage.setTo(user.getEmail());
        mailMessage.setSubject("Password Change Request");
        mailMessage.setText(String.format("""
                You have requested to change your password, please click here to continue:
                %s%s?token=%s

                Please, don't share this link with anyone.""", FRONTEND_URL, FRONTEND_USER_PASSWORD_CHANGE_PATH, token.getToken()));
        emailService.sendEmail(mailMessage);
    }

    public User validateTokenAndReturnCorrespondingUser(String token) {
        ConfirmationToken confirmationToken = confirmationTokenService.validateToken(token, TokenType.PASSWORD_CHANGE_TOKEN);
        User user = confirmationToken.getUser();
        confirmationTokenService.deleteToken(confirmationToken);
        return user;
    }

    public void deleteUserConfirmationTokenIfExists(User user) {
        confirmationTokenService.deleteUserConfirmationTokenIfExists(user, TokenType.PASSWORD_CHANGE_TOKEN);
    }
}
