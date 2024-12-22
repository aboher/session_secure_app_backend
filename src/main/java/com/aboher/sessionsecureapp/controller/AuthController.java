package com.aboher.sessionsecureapp.controller;

import com.aboher.sessionsecureapp.dto.AuthInfo;
import com.aboher.sessionsecureapp.dto.LoginRequest;
import com.aboher.sessionsecureapp.exception.InvalidCredentialsException;
import com.aboher.sessionsecureapp.service.AuthService;
import com.aboher.sessionsecureapp.service.SessionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.web.bind.annotation.*;

@RequiredArgsConstructor
@RestController
public class AuthController {

    private final AuthService usernamePasswordAuthService;
    private final SessionService sessionService;

    @Operation(
            description = """
                    Enables you to log in by providing your credentials in the
                    body of the request, like this:
                                        
                    ### Body example
                    ```json
                    {
                        "email": "john_doe@mail.com",
                        "password": "Password1!",
                    }
                    ```
                    """,
            security = @SecurityRequirement(name = "csrf-token")
    )
    @PostMapping("/login")
    @ResponseStatus(HttpStatus.OK)
    public void login(@RequestBody LoginRequest loginRequest) {
        Authentication authenticationRequest =
                UsernamePasswordAuthenticationToken.unauthenticated(
                        loginRequest.email(),
                        loginRequest.password());
        try {
            usernamePasswordAuthService.authenticate(authenticationRequest);
        } catch (BadCredentialsException e) {
            throw new InvalidCredentialsException("Invalid Credentials");
        }
    }

    @Operation(
            description = "Allows you to log out, after you have logged in",
            security = {
                    @SecurityRequirement(name = "session-cookie"),
                    @SecurityRequirement(name = "csrf-token")
            }
    )
    @PostMapping("/logout")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void logout(
            Authentication authentication, HttpServletRequest request, HttpServletResponse response) {
        SecurityContextLogoutHandler logoutHandler = new SecurityContextLogoutHandler();
        if (authentication != null) {
            logoutHandler.logout(request, response, authentication);
        }
    }

    @Operation(
            description = "Returns information about the logged in user",
            security = @SecurityRequirement(name = "session-cookie")
    )
    @GetMapping("/auth-info")
    public AuthInfo authorizationInfo() {
        return AuthInfo.builder()
                .email(sessionService.getEmail())
                .roles(sessionService.getRoles())
                .expirationDate(sessionService.getExpirationDate())
                .build();
    }
}
