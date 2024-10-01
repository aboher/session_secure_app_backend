package com.aboher.inventory.service;

import com.aboher.inventory.exception.InvalidTokenException;
import com.aboher.inventory.model.User;

public interface TokenBasedVerificationService {

    void sendMessageWithConfirmationToken(User user);

    User validateTokenAndReturnCorrespondingUser(String token) throws InvalidTokenException;

    void deleteUserConfirmationTokenIfExists(User user);
}
