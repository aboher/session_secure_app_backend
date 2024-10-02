package com.aboher.sessionsecureapp.service;

import com.aboher.sessionsecureapp.exception.InvalidTokenException;
import com.aboher.sessionsecureapp.model.User;

public interface TokenBasedVerificationService {

    void sendMessageWithConfirmationToken(User user);

    User validateTokenAndReturnCorrespondingUser(String token) throws InvalidTokenException;

    void deleteUserConfirmationTokenIfExists(User user);
}
