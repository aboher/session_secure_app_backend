package com.aboher.sessionsecureapp.controller;

import com.aboher.sessionsecureapp.dto.SessionInfo;
import com.aboher.sessionsecureapp.exception.InvalidAttributeException;
import com.aboher.sessionsecureapp.service.SessionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
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

    @Operation(
            description = """
                    Returns information about the session with id specified as a
                    query parameter. If no session id is specified, the information of the
                    session of the client making the request is returned.
                                        
                    **Requires Role USER** to see information from sessions belonging
                    to the same user making the request.
                                        
                    **Requires Role ADMIN** to see information from sessions belonging to any
                    logged in user.
                    """,
            security = {
                    @SecurityRequirement(name = "session-cookie")
            }
    )
    @GetMapping("info")
    public SessionInfo getSessionInfo(@RequestParam(defaultValue = "current-session") String id) {
        if ("current-session".equals(id)) {
            return sessionService.getSessionInfo();
        }
        return sessionService.getSessionInfoBySessionId(id);
    }

    @Operation(
            description = """
                    Specifies the time, in seconds, between client requests before
                    the session expires. A zero or negative time indicates that
                    the session should never timeout.
                                        
                    The time is specified as a query parameter named interval.
                                        
                    **Requires Role USER**
                    """,
            security = {
                    @SecurityRequirement(name = "session-cookie"),
                    @SecurityRequirement(name = "csrf-token")
            }
    )
    @PutMapping("max-inactive-interval")
    public void setSessionMaxInactiveInterval(@RequestParam int interval) {
        sessionService.setMaxInactiveInterval(interval);
    }

    @Operation(
            description = """
                    Returns an array with all the attributes of the user making
                    the request.
                                        
                    **Requires Role MODERATOR**
                    """,
            security = {
                    @SecurityRequirement(name = "session-cookie")
            }
    )
    @PreAuthorize("hasRole('MODERATOR')")
    @GetMapping("attributes")
    public Map<String, Object> getAttributes() {
        return sessionService.getAttributes();
    }

    @Operation(
            description = """
                    Returns the session's attribute with name specified in a query parameter.
                                        
                    **Requires Role MODERATOR**
                    """,
            security = {
                    @SecurityRequirement(name = "session-cookie")
            }
    )
    @PreAuthorize("hasRole('MODERATOR')")
    @GetMapping("attribute")
    public Object getAttribute(@RequestParam String name) {
        return sessionService.getAttribute(name);
    }

    @Operation(
            description = """
                    Store any object specified in the body of the request in an
                    attribute with name specified in the 'name' query parameter.
                                        
                    **Requires Role MODERATOR**
                    """,
            security = {
                    @SecurityRequirement(name = "session-cookie"),
                    @SecurityRequirement(name = "csrf-token")
            }
    )
    @PreAuthorize("hasRole('MODERATOR')")
    @PutMapping("attribute")
    public void setAttribute(@RequestParam String name, @RequestBody Object object) {
        if (canNotModifyAttribute(name)) {
            throw new InvalidAttributeException(
                    String.format("You are not allowed to modify '%s' attribute", name));
        }
        sessionService.setAttribute(name, object);
    }

    @Operation(
            description = """
                    Deletes the attribute specified in the 'name' query parameter.
                                        
                    **Requires Role MODERATOR**
                    """,
            security = {
                    @SecurityRequirement(name = "session-cookie"),
                    @SecurityRequirement(name = "csrf-token")
            }
    )
    @PreAuthorize("hasRole('MODERATOR')")
    @DeleteMapping("attribute")
    public void deleteAttribute(@RequestParam String name) {
        if (canNotModifyAttribute(name)) {
            throw new InvalidAttributeException(
                    String.format("You are not allowed to delete '%s' attribute", name));
        }
        sessionService.removeAttribute(name);
    }

    private boolean canNotModifyAttribute(String attrName) {
        return "SESSION_DETAILS".equals(attrName) || "SPRING_SECURITY_CONTEXT".equals(attrName);
    }

    @Operation(
            description = """
                    Returns an array of all the active session ids from the user
                    making the request.
                    """,
            security = {
                    @SecurityRequirement(name = "session-cookie")
            }
    )
    @GetMapping("active-sessions-ids")
    public Set<String> getAllActiveSessionsIds(Principal principal) {
        return sessionService.getAllActiveSessionsIds(principal);
    }

    @Operation(
            description = """
                    Invalidates the session with id specified as a query parameter.
                    **Requires Role USER** to invalidate his own sessions.
                    **Requires Role ADMIN** to invalidate sessions from any user.
                    """,
            security = {
                    @SecurityRequirement(name = "session-cookie"),
                    @SecurityRequirement(name = "csrf-token")
            }
    )
    @DeleteMapping("delete-session")
    public void deleteSession(@RequestParam String id) {
        sessionService.invalidateSession(id);
    }

    @Operation(
            description = """
                    Returns an array with the emails of all the users with active
                    sessions.
                                        
                    **Requires Role ADMIN**
                    """,
            security = {
                    @SecurityRequirement(name = "session-cookie")
            }
    )
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("emails-of-all-active-sessions")
    public Set<String> getPrincipalNamesOfAllTheUsersWithActiveSessions() {
        return sessionService.getPrincipalNamesOfAllTheUsersWithActiveSessions();
    }

    @Operation(
            description = """
                    Returns an array of all the active session ids from the user
                    with email specified in the path variable 'principalName'.
                                        
                    **Requires Role ADMIN**
                    """,
            security = {
                    @SecurityRequirement(name = "session-cookie")
            }
    )
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("active-sessions-ids/{principalName}")
    public Set<String> getAllActiveSessionsIds(@PathVariable String principalName) {
        return sessionService.getAllActiveSessionsIds(principalName);
    }
}
