package com.aboher.sessionsecureapp.controller;

import com.aboher.sessionsecureapp.dto.AuthInfo;
import com.aboher.sessionsecureapp.dto.LoginRequest;
import com.aboher.sessionsecureapp.service.AuthService;
import com.aboher.sessionsecureapp.service.SessionService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collections;
import java.util.Map;

@RequiredArgsConstructor
@RestController
public class AuthController {

    private final AuthService usernamePasswordAuthService;
    private final SessionService sessionService;

    @PostMapping("/login")
    public ResponseEntity<Map<String, String>> login(@RequestBody LoginRequest loginRequest) {
        Authentication authenticationRequest =
                UsernamePasswordAuthenticationToken.unauthenticated(
                        loginRequest.email(),
                        loginRequest.password());
        try {
            usernamePasswordAuthService.authenticate(authenticationRequest);
            return ResponseEntity.ok().build();
        } catch (BadCredentialsException e) {
            return ResponseEntity
                    .status(HttpStatus.UNAUTHORIZED)
                    .body(Collections.singletonMap("errorMessage", "Invalid Credentials"));
        }
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(
            Authentication authentication, HttpServletRequest request, HttpServletResponse response) {
        SecurityContextLogoutHandler logoutHandler = new SecurityContextLogoutHandler();
        if (authentication != null) {
            logoutHandler.logout(request, response, authentication);
        }
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

    @GetMapping("/auth-info")
    public AuthInfo authorizationInfo() {
        return AuthInfo.builder()
                .email(sessionService.getEmail())
                .roles(sessionService.getRoles())
                .expirationDate(sessionService.getExpirationDate())
                .build();
    }
}
