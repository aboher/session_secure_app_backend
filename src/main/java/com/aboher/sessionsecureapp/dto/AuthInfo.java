package com.aboher.sessionsecureapp.dto;

import com.aboher.sessionsecureapp.enums.Role;
import lombok.Builder;

import java.util.Date;
import java.util.Set;

@Builder
public record AuthInfo(String email,
                       Set<Role> roles,
                       Date expirationDate) {
}
