package com.aboher.sessionsecureapp.controller;

import com.aboher.sessionsecureapp.dto.SessionInfo;
import com.aboher.sessionsecureapp.exception.InvalidAttributeException;
import com.aboher.sessionsecureapp.service.impl.SessionService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.Map;
import java.util.Set;

@RestController
@RequiredArgsConstructor
@RequestMapping("session")
public class SessionController {

    private final SessionService sessionService;

    @GetMapping("info")
    public SessionInfo getSessionInfo(@RequestParam(defaultValue = "current-session") String id) {
        if ("current-session".equals(id)) {
            return sessionService.getSessionInfo();
        }
        return sessionService.getSessionInfoBySessionId(id);
    }

    @PutMapping("max-inactive-interval")
    public void setSessionMaxInactiveInterval(@RequestParam int interval) {
        sessionService.setMaxInactiveInterval(interval);
    }

    @GetMapping("attributes")
    public Map<String, Object> getAttributes() {
        return sessionService.getAttributes();
    }

    @GetMapping("attribute")
    public Object getAttribute(@RequestParam String name) {
        return sessionService.getAttribute(name);
    }

    @PutMapping("attribute")
    public void setAttribute(@RequestParam String name, @RequestBody Object object) {
        if ("SESSION_DETAILS".equals(name) || "SPRING_SECURITY_CONTEXT".equals(name)) {
            throw new InvalidAttributeException(
                    String.format("You are not allowed to modify '%s' attribute", name));
        }
        sessionService.setAttribute(name, object);
    }

    @DeleteMapping("attribute")
    public void deleteAttribute(@RequestParam String name) {
        if ("SESSION_DETAILS".equals(name) || "SPRING_SECURITY_CONTEXT".equals(name)) {
            throw new InvalidAttributeException(
                    String.format("You are not allowed to delete '%s' attribute", name));
        }
        sessionService.removeAttribute(name);
    }

    @GetMapping("active-sessions-ids")
    public Set<String> getAllActiveSessionsIds(Principal principal) {
        return sessionService.getAllActiveSessionsIds(principal);
    }

    @DeleteMapping("delete-session")
    public void deleteSession(@RequestParam String id) {
        sessionService.invalidateSession(id);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("emails-of-all-active-sessions")
    public Set<String> getAllActiveSessionsPrincipalNames() {
        return sessionService.getAllActiveSessionsPrincipalNames();
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("active-sessions-ids/{principalName}")
    public Set<String> getAllActiveSessionsIds(@PathVariable String principalName) {
        return sessionService.getAllActiveSessionsIds(principalName);
    }
}
