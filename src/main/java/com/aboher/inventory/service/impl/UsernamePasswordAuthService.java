package com.aboher.inventory.service.impl;

import com.aboher.inventory.service.AuthService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UsernamePasswordAuthService implements AuthService {

    private final AuthenticationManager authenticationManager;
    private final SessionService sessionService;

    @Override
    public void authenticate(Authentication authenticationRequest) throws BadCredentialsException {
        Authentication authenticationResponse =
                authenticationManager.authenticate(authenticationRequest);
        HttpSession newSession = sessionService.createNewSession();
        SecurityContextHolder.getContext().setAuthentication(authenticationResponse);
        newSession.setAttribute(
                HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY,
                SecurityContextHolder.getContext());
    }
}
