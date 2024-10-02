package com.aboher.sessionsecureapp.dto;

import com.aboher.sessionsecureapp.enums.Role;

import java.util.Date;
import java.util.Set;

public record AuthInfo(String email,
                       Set<Role> roles,
                       Date expirationDate) {
}
