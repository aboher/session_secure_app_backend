package com.aboher.sessionsecureapp.controller;

import com.aboher.sessionsecureapp.dto.UserDto;
import com.aboher.sessionsecureapp.mapper.Mapper;
import com.aboher.sessionsecureapp.model.User;
import com.aboher.sessionsecureapp.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@AllArgsConstructor
@RestController
@RequestMapping("/users")
public class UserController {

    private final UserService userService;
    private final Mapper<User, UserDto> userDtoMapper;

    @Operation(
            operationId = "create-user-operation-id",
            description = """
                    Creates a new user account. To enable it, you need to confirm the account at
                    [/users/confirm-account](#/user-controller/confirm-account-operation-id)
                    with the token received by email.
                                        
                    ### Body example
                    ```json
                    {
                        "firstName": "John",
                        "lastName": "Doe",
                        "email": "john_doe@mail.com",
                        "password": "Password1!",
                        "roles": ["ROLE_USER", "ROLE_MODERATOR"]
                    }
                    ```
                                        
                    ### Fields requirements
                                        
                    * **firstName**: No numbers allowed
                    * **lastName**: No numbers allowed
                    * **email**: Must be a valid email
                    * **password:** Must have between 8 to 24 characters. Must include an uppercase letter. Must include a lowercase letter. Must include a number. Must include a special character. Allowed special Characters are: ! @ # $ %
                    * **roles:** Must be an array of all the roles assigned, the possible values are: "ROLE_USER", "ROLE_MODERATOR", "ROLE_ADMIN"
                    """
    )
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public UserDto createUser(@RequestBody UserDto newUserDto) {
        User newUser = userDtoMapper.toEntity(newUserDto);
        User createdUser = userService.createUser(newUser);
        return userDtoMapper.toDto(createdUser);
    }

    @Operation(
            operationId = "confirm-account-operation-id",
            description = """
                    Confirm user account after registration if the token is the
                    one received by email. This endpoint must be used after
                    creating an account at
                    [/users](#/user-controller/create-user-operation-id)
                    """
    )
    @GetMapping("/confirm-account")
    public void confirmUserAccount(@RequestParam("token") String confirmationToken) {
        userService.validateTokenAndEnableUser(confirmationToken);
    }

    @Operation(
            operationId = "request-password-change-operation-id",
            description = """
                    Sends an email, to the email query parameter, with a link with
                    a query parameter, named token, to change the password. For
                    the password to be change you need to confirm the operation at
                    [/users/password-change](#/user-controller/password-change-operation-id)
                    using the token received.
                    """
    )
    @PostMapping("/request-password-change")
    public void requestPasswordChange(@RequestParam("email") String email) {
        userService.requestPasswordChange(email);
    }

    @Operation(
            operationId = "password-change-operation-id",
            description = """
                    Change the password for the one specified in the "password"
                    key of the body of the request:
                                        
                    <pre>
                    <code>
                    {
                        "password": "NewPassword1!"
                    }
                    </code>
                    </pre>
                                        
                    For the password to be changed correctly, the "token" query
                    parameter must be the one received by email. To get one, you
                    must make a request at
                    [/users/request-password-change](#/user-controller/request-password-change-operation-id)
                    """
    )
    @PatchMapping("/password-change")
    public void changePassword(@RequestParam("token") String token, @RequestBody UserDto userDto) {
        User user = userDtoMapper.toEntity(userDto);
        userService.validateTokenAndChangePassword(token, user.getPassword());
    }

    @Operation(
            operationId = "request-account-deletion-operation-id",
            description = """
                    Sends a link with a token query parameter to delete your
                    account. For the account to be deleted you need to confirm
                    the operation at
                    [delete-account](#/user-controller/delete-account-operation-id)
                    using the token received.
                    """
    )
    @PostMapping("/request-account-deletion")
    public void requestAccountDeletion() {
        userService.requestAccountDeletion();
    }

    @Operation(
            operationId = "delete-account-operation-id",
            description = """
                    Deletes user account if the token specified is verified to be correct.
                    **Requires Role USER**
                                        
                    For the account to be deleted correctly, the "token" query
                    parameter must be the one received by email. To get one, you
                    must make a request at
                    [/users/request-account-deletion](#/user-controller/request-account-deletion-operation-id)
                    """,
            security = @SecurityRequirement(name = "SESSION")
    )
    @DeleteMapping("/delete-account")
    public void deleteAccount(@RequestParam("token") String token) {
        userService.validateTokenAndDeleteAccount(token);
    }
}
