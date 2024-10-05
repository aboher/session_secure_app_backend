package com.aboher.sessionsecureapp.service.impl;

import com.aboher.sessionsecureapp.dto.SessionInfo;
import com.aboher.sessionsecureapp.enums.Role;
import com.aboher.sessionsecureapp.exception.InvalidSessionException;
import com.aboher.sessionsecureapp.model.SessionDetails;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.session.FindByIndexNameSessionRepository;
import org.springframework.session.Session;
import org.springframework.stereotype.Service;

import java.security.Principal;
import java.time.Duration;
import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SessionService {

    private final String SESSION_DETAILS = "SESSION_DETAILS";

    private final HttpServletRequest request;
    private final HttpSession httpSession;
    private final FindByIndexNameSessionRepository<? extends Session> sessionRepository;

    public HttpSession invalidateOldSessionAndCreateNewOne() {
        HttpSession oldSession = request.getSession(false);
        if (oldSession != null) {
            oldSession.invalidate();
        }
        return request.getSession(true);
    }

    public Date getExpirationDate() {
        int maxInactiveInterval = httpSession.getMaxInactiveInterval();
        long expirationTime = httpSession.getLastAccessedTime() + (maxInactiveInterval * 1000L);
        return new Date(expirationTime);
    }

    public Date getExpirationDate(HttpSession httpSession) {
        int maxInactiveInterval = httpSession.getMaxInactiveInterval();
        long expirationTime = httpSession.getLastAccessedTime() + (maxInactiveInterval * 1000L);
        return new Date(expirationTime);
    }

    public Date getExpirationDate(Session session) {
        Duration duration = session.getMaxInactiveInterval();
        Instant instant = session.getLastAccessedTime().plus(duration);
        return Date.from(instant);
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

    public Date getCreationDate(HttpSession httpSession) {
        return new Date(httpSession.getCreationTime());
    }

    public Date getLastAccessedDate(HttpSession httpSession) {
        return new Date(httpSession.getLastAccessedTime());
    }

    public void setMaxInactiveInterval(int interval) {
        httpSession.setMaxInactiveInterval(interval);
    }

    public Map<String, Object> getAttributes() {
        Map<String, Object> attributes = new HashMap<>();
        for (Enumeration<String> attributeNames = httpSession.getAttributeNames(); attributeNames.hasMoreElements(); ) {
            String attributeName = attributeNames.nextElement();
            attributes.put(attributeName, httpSession.getAttribute(attributeName));
        }
        return attributes;
    }

    public Object getAttribute(String name) {
        return httpSession.getAttribute(name);
    }

    public void setAttribute(String name, Object value) {
        httpSession.setAttribute(name, value);
    }

    public void removeAttribute(String name) {
        httpSession.removeAttribute(name);
    }

    public Set<String> getAllActiveSessionsIds(Principal principal) {
        return sessionRepository.findByPrincipalName(principal.getName()).values().stream()
                .map(Session::getId)
                .collect(Collectors.toSet());
    }

    public SessionInfo getSessionInfoBySessionId(String id) {
        Session session = sessionRepository.findById(id);
        if (session == null) {
            throw new InvalidSessionException("There is no session with id = " + id);
        }
        return getSessionInfo(session);
    }

    public SessionInfo getSessionInfo(Session session) {
        return SessionInfo.builder()
                .id(session.getId())
                .creationDate(getCreationDate(session))
                .lastAccessedDate(Date.from(session.getLastAccessedTime()))
                .expirationDate(getExpirationDate(session))
                .sessionDetails(session.getAttribute(SESSION_DETAILS))
                .build();
    }

    public void storeSessionDetailsInAttributes() {
        SessionDetails sessionDetails = SessionDetails.builder()
                .remoteAddress(request.getRemoteAddr())
                .userAgent(request.getHeader("User-Agent"))
                .build();
        setAttribute(SESSION_DETAILS, sessionDetails);
    }

    private Date getCreationDate(Session session) {
        return Date.from(session.getCreationTime());
    }


    public SessionInfo getSessionInfo(HttpSession httpSession) {
        return SessionInfo.builder()
                .id(httpSession.getId())
                .creationDate(getCreationDate(httpSession))
                .lastAccessedDate(getLastAccessedDate(httpSession))
                .expirationDate(getExpirationDate(httpSession))
                .sessionDetails((SessionDetails) httpSession.getAttribute(SESSION_DETAILS))
                .build();
    }

    public SessionInfo getSessionInfo() {
        return getSessionInfo(httpSession);
    }
}
