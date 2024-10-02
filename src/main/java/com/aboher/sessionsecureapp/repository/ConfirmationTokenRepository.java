package com.aboher.sessionsecureapp.repository;

import com.aboher.sessionsecureapp.enums.TokenType;
import com.aboher.sessionsecureapp.model.ConfirmationToken;
import com.aboher.sessionsecureapp.model.User;
import org.springframework.data.repository.CrudRepository;

public interface ConfirmationTokenRepository extends CrudRepository<ConfirmationToken, Long> {
    ConfirmationToken findByToken(String confirmationToken);

    ConfirmationToken findByUserAndTokenType(User user, TokenType tokenType);
}
