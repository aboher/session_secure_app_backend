package com.aboher.sessionsecureapp.service;

import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;

public interface AuthService {
    void authenticate(Authentication authentication) throws BadCredentialsException;
}
