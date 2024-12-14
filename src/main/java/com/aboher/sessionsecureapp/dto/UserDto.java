package com.aboher.sessionsecureapp.dto;

import com.aboher.sessionsecureapp.enums.Role;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;

import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.REQUIRED;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UserDto {
    @Schema(description = "User first name", example = "John", requiredMode = REQUIRED)
    private String firstName;
    @Schema(description = "User last name", example = "Doe", requiredMode = REQUIRED)
    private String lastName;
    @Schema(description = "User email", example = "john_doe@mail.com", requiredMode = REQUIRED)
    private String email;
    @Schema(
            description = """
                    User password. Valid password must have between 8 to 24 characters.
                    Must include uppercase and lowercase letters, a number and a special
                    character. Allowed special Characters: !@#$%
                    """,
            example = "examplePassword1!",
            requiredMode = REQUIRED)
    private String password;
    @ArraySchema(
            schema = @Schema(
                    implementation = Role.class,
                    description = "User roles (at least one required)",
                    enumAsRef = true),
            uniqueItems = true,
            minItems = 1
    )
    private Set<Role> roles;
    private boolean enabled;
}
