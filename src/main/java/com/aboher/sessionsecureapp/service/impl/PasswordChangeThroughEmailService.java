package com.aboher.sessionsecureapp.service.impl;

import com.aboher.sessionsecureapp.config.FrontendProperties;
import com.aboher.sessionsecureapp.enums.TokenType;
import com.aboher.sessionsecureapp.model.ConfirmationToken;
import com.aboher.sessionsecureapp.model.User;
import com.aboher.sessionsecureapp.service.MessageSender;
import com.aboher.sessionsecureapp.service.TokenBasedVerificationService;
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
