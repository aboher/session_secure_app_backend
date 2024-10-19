package com.aboher.sessionsecureapp.service.verification;

import com.aboher.sessionsecureapp.enums.TokenType;
import com.aboher.sessionsecureapp.exception.InvalidTokenException;
import com.aboher.sessionsecureapp.model.ConfirmationToken;
import com.aboher.sessionsecureapp.model.User;
import com.aboher.sessionsecureapp.repository.ConfirmationTokenRepository;
import lombok.Data;
import org.springframework.stereotype.Service;

@Service
@Data
public class ConfirmationTokenService {

    private final ConfirmationTokenRepository confirmationTokenRepository;

    public ConfirmationToken createToken(User user, TokenType tokenType, int expirationTimePeriodInMinutes) {
        ConfirmationToken token = new ConfirmationToken(user, tokenType, expirationTimePeriodInMinutes);
        return confirmationTokenRepository.save(token);
    }

    public ConfirmationToken validateToken(String token, TokenType tokenType) {
        ConfirmationToken confirmationToken = confirmationTokenRepository.findByToken(token);
        if (confirmationToken == null) {
            throw new InvalidTokenException("The token is not valid");
        }
        if (confirmationToken.hasTokenExpired()) {
            confirmationTokenRepository.delete(confirmationToken);
            throw new InvalidTokenException("The token has expired");
        }
        if (!confirmationToken.getTokenType().equals(tokenType)) {
            throw new InvalidTokenException("The token is not valid");
        }
        return confirmationToken;
    }

    public void deleteToken(ConfirmationToken confirmationToken) {
        confirmationTokenRepository.delete(confirmationToken);
    }

    public void deleteUserConfirmationTokenIfExists(User user, TokenType tokenType) {
        ConfirmationToken token = confirmationTokenRepository.findByUserAndTokenType(user, tokenType);
        if (token != null) {
            confirmationTokenRepository.delete(token);
        }
    }
}
