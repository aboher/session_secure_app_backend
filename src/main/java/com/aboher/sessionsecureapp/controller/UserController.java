package com.aboher.sessionsecureapp.controller;

import com.aboher.sessionsecureapp.dto.ErrorMessage;
import com.aboher.sessionsecureapp.dto.UserDto;
import com.aboher.sessionsecureapp.mapper.Mapper;
import com.aboher.sessionsecureapp.model.User;
import com.aboher.sessionsecureapp.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
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
            summary = "Creates new user",
            description = "Creates new user if one with the same email doesn't exist yet.")
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "201",
                    description = """
                            Creates a new user account. To enable it, you need to confirm the account at
                            <a target="_blank" href='index.html#/user-controller/confirm_account_operation_id'>/users/confirm-account</a>
                            with the token received by email. The token has a validity period of one day.
                            """,
                    content = {@Content(mediaType = "application/json",
                            schema = @Schema(implementation = UserDto.class))}),
            @ApiResponse(
                    responseCode = "400",
                    description = """
                            User creation failed, generally because the user information supplied
                            is not valid. All other types of errors, for example, when the user
                            is already created with the same email, is notified only through email,
                            to avoid giving away the information of whether a given email has an
                            account or not.
                            """,
                    content = {@Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorMessage.class))})
    })
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public UserDto createUser(@RequestBody UserDto newUserDto) {
        User newUser = userDtoMapper.toEntity(newUserDto);
        User createdUser = userService.createUser(newUser);
        return userDtoMapper.toDto(createdUser);
    }

    @Operation(operationId = "confirm_account_operation_id",
            summary = "Confirm user account after registration",
            description = "Confirm user account after registration if the token is the one received by email.")
    @GetMapping("/confirm-account")
    public void confirmUserAccount(@RequestParam("token") String confirmationToken) {
        userService.validateTokenAndEnableUser(confirmationToken);
    }

    @PostMapping("/request-password-change")
    public void requestPasswordChange(@RequestParam("email") String email) {
        userService.requestPasswordChange(email);
    }

    @PatchMapping("/password-change")
    public void changePassword(@RequestParam("token") String token, @RequestBody UserDto userDto) {
        User user = userDtoMapper.toEntity(userDto);
        userService.validateTokenAndChangePassword(token, user.getPassword());
    }

    @PostMapping("/request-account-deletion")
    public void requestAccountDeletion() {
        userService.requestAccountDeletion();
    }

    @DeleteMapping("/delete-account")
    public void deleteAccount(@RequestParam("token") String token) {
        userService.validateTokenAndDeleteAccount(token);
    }
}
