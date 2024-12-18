package com.aboher.sessionsecureapp.dto;

import com.aboher.sessionsecureapp.enums.Role;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UserDto {
    private String firstName;
    private String lastName;
    private String email;
    private String password;
    private Set<Role> roles;
    private boolean enabled;
}
