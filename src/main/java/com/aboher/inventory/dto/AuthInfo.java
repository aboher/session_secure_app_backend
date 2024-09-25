package com.aboher.inventory.dto;

import com.aboher.inventory.enums.Role;

import java.util.Date;
import java.util.Set;

public record AuthInfo(String email,
                       Set<Role> roles,
                       Date expirationDate) {
}
