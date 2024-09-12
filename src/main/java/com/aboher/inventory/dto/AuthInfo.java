package com.aboher.inventory.dto;

import java.util.Date;
import java.util.List;

public record AuthInfo(String username,
                       List<String> roles,
                       Date expirationDate) {
}
