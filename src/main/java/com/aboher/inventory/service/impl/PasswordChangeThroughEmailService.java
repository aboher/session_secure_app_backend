package com.aboher.inventory.service.impl;

import com.aboher.inventory.config.FrontendProperties;
import com.aboher.inventory.enums.TokenType;
import com.aboher.inventory.model.ConfirmationToken;
import com.aboher.inventory.model.User;
import com.aboher.inventory.service.MessageSender;
import com.aboher.inventory.service.TokenBasedVerificationService;
import lombok.Data;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.stereotype.Service;

@Service
@Data
public class PasswordChangeThroughEmailService implements TokenBasedVerificationService {
    private final int EXPIRATION_TIME_PERIOD_IN_MINUTES = 10;
    private final FrontendProperties frontendProperties;
    private final ConfirmationTokenService confirmationTokenService;
    private final MessageSender<SimpleMailMessage> emailMessageSender;

    @Override
    public void sendMessageWithConfirmationToken(User user) {
        ConfirmationToken token = confirmationTokenService.createToken(user,
                TokenType.PASSWORD_CHANGE_TOKEN,
                EXPIRATION_TIME_PERIOD_IN_MINUTES);

        SimpleMailMessage mailMessage = new SimpleMailMessage();
        mailMessage.setTo(user.getEmail());
        mailMessage.setSubject("Password Change Request");
        mailMessage.setText(String.format("""
                        You have requested to change your password, please click here to continue:
                        %s%s?token=%s

                        Please, don't share this link with anyone.""",
                frontendProperties.getUrl(),
                frontendProperties.getUserPasswordChangePath(),
                token.getToken()));
        emailMessageSender.sendMessage(mailMessage);
    }

    @Override
    public User validateTokenAndReturnCorrespondingUser(String token) {
        ConfirmationToken confirmationToken =
                confirmationTokenService.validateToken(token, TokenType.PASSWORD_CHANGE_TOKEN);
        User user = confirmationToken.getUser();
        confirmationTokenService.deleteToken(confirmationToken);
        return user;
    }

    @Override
    public void deleteUserConfirmationTokenIfExists(User user) {
        confirmationTokenService.deleteUserConfirmationTokenIfExists(user, TokenType.PASSWORD_CHANGE_TOKEN);
    }
}
