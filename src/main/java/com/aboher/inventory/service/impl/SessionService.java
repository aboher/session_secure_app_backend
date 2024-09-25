package com.aboher.inventory.service.impl;

import com.aboher.inventory.enums.Role;
import com.aboher.inventory.exception.InvalidSessionException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.Date;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SessionService {

    private final HttpServletRequest request;

    public HttpSession createNewSession() {
        HttpSession oldSession = request.getSession(false);
        if (oldSession != null) {
            oldSession.invalidate();
        }
        return request.getSession(true);
    }

    public Date getSessionExpirationDate() throws InvalidSessionException {
        HttpSession session = request.getSession(false);
        if (session != null) {
            int maxInactiveInterval = session.getMaxInactiveInterval();
            long expirationTime = session.getLastAccessedTime() + (maxInactiveInterval * 1000L);
            return new Date(expirationTime);
        }
        throw new InvalidSessionException("Session doesn't exist");
    }

    public String getEmail() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return authentication.getName();
    }

    public Set<Role> getRoles() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        Collection<? extends GrantedAuthority> authorities = authentication.getAuthorities();
        return authorities.stream()
                .map(authority -> Role.valueOf(authority.getAuthority()))
                .collect(Collectors.toSet());
    }
}
