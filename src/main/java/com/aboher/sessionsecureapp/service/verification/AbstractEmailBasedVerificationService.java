package com.aboher.sessionsecureapp.service.verification;

import com.aboher.sessionsecureapp.enums.TokenType;
import com.aboher.sessionsecureapp.exception.InvalidTokenException;
import com.aboher.sessionsecureapp.model.ConfirmationToken;
import com.aboher.sessionsecureapp.model.User;
import com.aboher.sessionsecureapp.service.MessageSender;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;

@RequiredArgsConstructor
public abstract class AbstractEmailBasedVerificationService implements TokenBasedVerificationService {

    @Value("${spring.mail.username}")
    private String smtpEmail;

    private final ConfirmationTokenService confirmationTokenService;
    private final MessageSender<SimpleMailMessage> emailMessageSender;

    protected abstract int getExpirationTimePeriodInMinutes();

    protected abstract TokenType getTokenType();

    protected abstract String getVerificationEmailSubject();

    protected abstract String getVerificationEmailText(String token);

    @Override
    public void sendMessageWithConfirmationToken(User user) {
        ConfirmationToken token = confirmationTokenService.createToken(user, getTokenType(), getExpirationTimePeriodInMinutes());

        SimpleMailMessage mailMessage = new SimpleMailMessage();
        mailMessage.setFrom(smtpEmail);
        mailMessage.setTo(user.getEmail());
        mailMessage.setSubject(getVerificationEmailSubject());
        mailMessage.setText(getVerificationEmailText(token.getToken()));
        emailMessageSender.sendMessage(mailMessage);
    }

    @Override
    public User validateTokenAndReturnCorrespondingUser(String token) throws InvalidTokenException {
        ConfirmationToken confirmationToken = confirmationTokenService.validateToken(token, getTokenType());
        User user = confirmationToken.getUser();
        confirmationTokenService.deleteToken(confirmationToken);
        return user;
    }

    @Override
    public void deleteConfirmationTokenIfExists(User user) {
        confirmationTokenService.deleteUserConfirmationTokenIfExists(user, getTokenType());
    }
}
