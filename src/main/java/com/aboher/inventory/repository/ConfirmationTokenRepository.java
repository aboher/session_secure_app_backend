package com.aboher.inventory.repository;

import com.aboher.inventory.enums.TokenType;
import com.aboher.inventory.model.ConfirmationToken;
import com.aboher.inventory.model.User;
import org.springframework.data.repository.CrudRepository;

public interface ConfirmationTokenRepository extends CrudRepository<ConfirmationToken, Long> {
    ConfirmationToken findByToken(String confirmationToken);

    ConfirmationToken findByUserAndTokenType(User user, TokenType tokenType);
}
