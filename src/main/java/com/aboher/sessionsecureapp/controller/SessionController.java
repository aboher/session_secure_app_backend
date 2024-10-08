package com.aboher.sessionsecureapp.controller;

import com.aboher.sessionsecureapp.dto.SessionInfo;
import com.aboher.sessionsecureapp.service.impl.SessionService;
import lombok.RequiredArgsConstructor;
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
    public SessionInfo getSessionInfo(@RequestParam String id) {
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
        sessionService.setAttribute(name, object);
    }

    @DeleteMapping("attribute")
    public void deleteAttribute(@RequestParam String name) {
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
}
